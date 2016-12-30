package com.perblo.payment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.perblo.hostel.entitymanager.HostelEntityManagerImpl;
import com.perblo.payment.constants.TransactionStatus;
import com.perblo.payment.constants.TransactionTypeConstant;
import com.perblo.payment.entity.PaymentItem;
import com.perblo.payment.entity.PaymentTransaction;
import com.perblo.payment.entity.Pin;
import com.perblo.payment.entity.TransactionType;
import com.perblo.hostel.entity.HostelApplication;
import com.perblo.hostel.service.HostelApplicationStatus;

import java.io.Serializable;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.persistence.EntityTransaction;

import org.apache.log4j.Logger;

//import com.perblo.transcript.interswitch.Webpay;
//import com.perblo.transcript.interswitch.WebpaySoap;

@ManagedBean(name="paymentBean")
@SessionScoped
public class PaymentRequestBean implements Serializable {
    
    private static final Logger log = Logger.getLogger(PaymentRequestBean.class);

    @ManagedProperty(value = "#{hostelEntityManager}")
    HostelEntityManagerImpl hostelEntityManager;

    private EntityManager entityManager;
    
    @ManagedProperty(value="#{paymentService}")
    private PaymentService paymentService;
    
    private List<TransactionType> transactionTypes;        
    private TransactionType transactionType;            
    private long transactionTypeId;
    
    private List<PaymentItem> paymentItems  = new ArrayList<PaymentItem>();    
    private String payeeIdentifier;
    private double totalPaymentAmount;
    private double transactionCost;
    private double totalTransactionAmount;
    private PaymentTransaction paymentTransaction;
    private String paymentItemName;
    
    private boolean interswitchPayment;    
    private boolean etranzactPayment;    
    private boolean pinPayment;
        
    private String interswitchMertId;        
    private String interswitchCadpId;
    private String interswitchTranxRef;
    private String interswitchPaymentUrl;        
    private String interswitchResponse;
    private String interswitchResponseCode;
    private String interswitchResponseMessage;
    private double interswitchResponseAmount;
    
    private String pinNumber;    
    private boolean paymentSuccessful;    
    HostelApplication hostelApplication = null;
    
    public PaymentRequestBean() {
        this.entityManager = hostelEntityManager.getEntityManager();
        log.info("entityManager: " + this.entityManager.toString());   
    }
                
    public String processPayment(String payeeIdentifier, double totalPaymentAmount, String paymentItemName) {
    	this.totalPaymentAmount = totalPaymentAmount;
    	this.paymentItemName = paymentItemName;
    	this.payeeIdentifier = payeeIdentifier;
    	log.info("paymentItemName = " + paymentItemName);

        return "selecttransactiontype";
        //check previous payment status
        //verifyInterswitchPayment(payeeIdentifier);
    }

    private void verifyInterswitchPayment(String payeeIdentifier) {
        log.info("verifyInterswitchPayment = " + payeeIdentifier);
        Query query = entityManager.createNamedQuery("getHostelApplicationByApplicationNumber");
    	query.setParameter(1, payeeIdentifier);
    	hostelApplication = (HostelApplication)query.getSingleResult();
    	if(hostelApplication.getPaymentTransactionId() != 0) {
            paymentTransaction = entityManager.find(PaymentTransaction.class, hostelApplication.getPaymentTransactionId());
            if(paymentTransaction != null) {
                if(paymentTransaction.getStatus() == TransactionStatus.INITIALIZED) {
                    if(paymentTransaction.getTransactionType().getName().equalsIgnoreCase(TransactionTypeConstant.INTERSWITCH)) {
                        getPaymentSettings();
                        getInterswitchPaymentStatus(paymentTransaction.getTransactionId());
                        updateInterswitchPaymentStatus();
                    }
                }
            }
        }
    }
    
    public String selectTransactionType() {
    	String tranType = "";
    	//set payment type
        transactionType = entityManager.find(TransactionType.class, transactionTypeId);
        if(transactionType != null) {
            if(transactionType.getName().equalsIgnoreCase(TransactionTypeConstant.ETRANZACT)) {
                    initEtranzactPayment();   
                    tranType = TransactionTypeConstant.ETRANZACT;
            } else if(transactionType.getName().equalsIgnoreCase(TransactionTypeConstant.INTERSWITCH)) {
                    initInterswitchPayment();    
                    tranType = TransactionTypeConstant.INTERSWITCH;
            } else if(transactionType.getName().equalsIgnoreCase(TransactionTypeConstant.PIN)) {
                    initPinPayment();
                    //tranType = TransactionTypeConstant.PIN;
                    tranType = "pinpayment";
            }
        } else {
            log.warn("transactionType is null " + transactionTypeId);
        }
    	
    	return tranType;
    	    	
    }

    private void initEtranzactPayment() {
    	setEtranzactPayment(true);
    }

    private void initInterswitchPayment() {
        log.info("initInterswitchPayment");
    	setInterswitchPayment(true);
        //TODO
    	//setInterswitchTranxRef(org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(12));
    	setPaymentItem();
    	getPaymentSettings();

    	createPaymentTransaction(getInterswitchTranxRef());
        log.info("InterswitchTranxRef = "+ getInterswitchTranxRef());
    }

    private void initPinPayment() {
    	setPinPayment(true);
    	setPaymentItem();
    	getPaymentSettings();

    }
    
    
    public void createPaymentTransaction(String transactionId){
    	
        log.info("createPaymentTransaction()");
        //if(paymentTransaction == null) {

            Calendar calendar = Calendar.getInstance();
            paymentTransaction = new PaymentTransaction();
            paymentTransaction.setAmount(totalPaymentAmount);
            paymentTransaction.setDescription(this.getPaymentItemName());
            paymentTransaction.setPaymentDate(calendar.getTime());
            paymentTransaction.setReferenceId(this.getPayeeIdentifier());
            paymentTransaction.setStatus(TransactionStatus.INITIALIZED);
            paymentTransaction.setStatusMessage("");
            paymentTransaction.setTransactionId(transactionId);
            paymentTransaction.setTransactionType(this.getTransactionType());
            entityManager.persist(paymentTransaction);
            
        //}

        Query query = entityManager.createNamedQuery("getTranscriptRequestByRequestNumber");
    	query.setParameter(1, this.getPayeeIdentifier());
    	List results = query.getResultList();
    	if(results.size() > 0) {
            hostelApplication = (HostelApplication)results.get(0);
    	}
    	if(hostelApplication != null) {
            hostelApplication.setPaymentStatus(HostelApplicationStatus.NOT_PAID);
            hostelApplication.setPaymentTransactionId(paymentTransaction.getId());
            entityManager.merge(hostelApplication);
    	}
    }
   
    public void setPaymentItem() {
    	//set payment item
    	PaymentItem paymentItem = new PaymentItem();
    	paymentItem.setName(paymentItemName);
    	paymentItem.setAmount(totalPaymentAmount);
    	paymentItem.setDescription("Payment for Hostel accommodation");
    	paymentItems.add(paymentItem);

    }
    
    public String obtainPaymentURL(String paymentRequestUrl) {
        
    	String paymentUrl = "";
    	paymentUrl = paymentRequestUrl.substring(0,paymentRequestUrl.lastIndexOf("/")+1);    	
    	    	
    	if(transactionType.getName().equalsIgnoreCase(TransactionTypeConstant.INTERSWITCH)) {
    		paymentUrl += "interswitchpayment.seam";
    	} 
    	
    	return paymentUrl;
    }

    public String obtainInterswitchPaymentURL() {

        double interswitchAmount = totalPaymentAmount * 100;
    	String interswitchUrl = "";
    	interswitchUrl += this.getInterswitchPaymentUrl() + "?";
        interswitchUrl += "CADPID="+this.getInterswitchCadpId()+"&MERTID="+this.getInterswitchMertId()+
                "&TXNREF="+this.getInterswitchTranxRef()+"&AMT="+interswitchAmount+"&TRANTYPE=00";

        log.info("interswitchUrl = " + interswitchUrl);
    	return interswitchUrl;
    }
    
    public void getPaymentSettings() {
    	    	
    	//get interswitch settings
    	interswitchMertId = paymentService.getPaymentSettingByName(PaymentService.INTERSWITCH_MERTID);
    	interswitchCadpId = paymentService.getPaymentSettingByName(PaymentService.INTERSWITCH_CADPID);
    	interswitchPaymentUrl = paymentService.getPaymentSettingByName(PaymentService.INTERSWITCH_PAYMENTURL);
    	    	
    }
    
    private void getInterswitchPaymentStatus() {
        try {
            /*
        URL url = new URL("http://webpay.interswitchng.com/webpayservice_pilot/webpay.asmx?wsdl");
        QName qname = new QName("http://webpay.interswitchng.com/webpay/", "webpay");
        Webpay webPayservice = new Webpay(url, qname);
        WebpaySoap endpoint = (WebpaySoap) webPayservice.getWebpaySoap();

        log.info("getInterswitchPaymentStatus() interswitchCadpId = " + interswitchCadpId);
        log.info("getInterswitchPaymentStatus() interswitchMertId = " + interswitchMertId);
        log.info("getInterswitchPaymentStatus() getInterswitchTranxRef = " + getInterswitchTranxRef());
        setInterswitchResponse(endpoint.getStatus(interswitchCadpId, interswitchMertId, this.getInterswitchTranxRef()));

        log.info("getInterswitchResponse() = " + this.getInterswitchResponse());
                       
             * 
             */
        } catch(Exception e) {
            log.error("Exception in getInterswitchPaymentStatus()", e);
        }
     }

    private void getInterswitchPaymentStatus(String tranxRef) {
        try {
            /*
        URL url = new URL("http://webpay.interswitchng.com/webpayservice_pilot/webpay.asmx?wsdl");
        QName qname = new QName("http://webpay.interswitchng.com/webpay/", "webpay");
        Webpay webPayservice = new Webpay(url, qname);
        WebpaySoap endpoint = (WebpaySoap) webPayservice.getWebpaySoap();
        setInterswitchResponse("");
        log.info("getInterswitchPaymentStatus() interswitchCadpId = " + interswitchCadpId);
        log.info("getInterswitchPaymentStatus() interswitchMertId = " + interswitchMertId);

        setInterswitchResponse(endpoint.getStatus(interswitchCadpId, interswitchMertId, tranxRef));

        log.info("getInterswitchResponse() = " + this.getInterswitchResponse());
             * 
             */

        } catch(Exception e) {
            log.error("Exception in getInterswitchPaymentStatus()", e);
        }
     }

    private String getTransResponseCode() {
        String responseCode = "";        
        String parsedResponse = this.getInterswitchResponse();
        parsedResponse.replace("&",";;");
        parsedResponse.replace("amp;","");
        log.info("parsedResponse = " + parsedResponse);

        String[] responseArray = parsedResponse.split(";;");
        log.info("responseArray = " + responseArray);
        if(responseArray.length == 8) {
            String responsecode = responseArray[6];
            String[] codeArray = responsecode.split("=");
            log.info("codeArray = " + codeArray);
            responseCode = codeArray[1];
        }

        if(responseArray.length == 2) {
            String responsecode = responseArray[0];
            String[] codeArray = responsecode.split("=");
            log.info("codeArray = " + codeArray);
            responseCode = codeArray[7];
        }
        log.info("responseCode = " + responseCode);
        this.setInterswitchResponseCode(responseCode);
        return responseCode;
    }

    private double getTransResponseAmount() {
        double responseAmount = 0.0;
        String parsedResponse = this.getInterswitchResponse();
        parsedResponse.replace("&",";;");
        parsedResponse.replace("amp;","");
        log.info("parsedResponse = " + parsedResponse);

        String[] responseArray = parsedResponse.split(";;");
        log.info("responseArray = " + responseArray);
        if(responseArray.length == 8) {
            String responsecode = responseArray[5];
            String[] codeArray = responsecode.split("=");
            log.info("codeArray = " + codeArray);
            responseAmount = Double.parseDouble(codeArray[1]);
        }

        if(responseArray.length == 2) {
            String responsecode = responseArray[0];
            String[] codeArray = responsecode.split("=");
            log.info("codeArray = " + codeArray);
            responseAmount = Double.parseDouble(codeArray[6]);
        }
        log.info("responseAmount = " + responseAmount);

        this.setInterswitchResponseAmount(responseAmount);
        return responseAmount;
    }

    public void updateInterswitchPaymentStatus() {
        //for tests
        //failed transaction
        /*
        log.info(":::::::: Payment NOT Successful");
        this.setInterswitchResponseCode("39");
        paymentSuccessful = false;
        this.setInterswitchResponseMessage(paymentService.getInterswitchResponseCodeMessage(getInterswitchResponseCode()));
        paymentTransaction.setStatus(TransactionStatus.FAILED);
        paymentTransaction.setStatusMessage(this.getInterswitchResponseCode() + ":" +
                this.getInterswitchResponseMessage());
        entityManager.merge(paymentTransaction);
        */
        
        log.info(":::::::: Payment Successful");
        //paymentSuccessful = true;
        this.setPaymentSuccessful(true);
        paymentTransaction.setStatus(TransactionStatus.SUCCESSFUL);
        paymentTransaction.setStatusMessage("Successful");
        entityManager.merge(paymentTransaction);

        hostelApplication.setPaymentStatus(HostelApplicationStatus.PAID);
        entityManager.merge(hostelApplication);

        //facesMessages.add("Payment was successful. Thank you for applying for your transcript online.");
        //facesMessages.add("Your transcript request has been saved, you can check your request status with your Student Number(" +
        //                    hostelApplication.getStudentNumber() + ") and Request Number(" + hostelApplication.getApplicationNumber() + ")");
		
        /*
        getInterswitchPaymentStatus();
        getTransResponseCode();
        getTransResponseAmount();

        if(this.getInterswitchResponseCode().equalsIgnoreCase("00") &&
                this.getInterswitchResponseAmount()==this.getTotalPaymentAmount()) {
            log.info(":::::::: Payment Successful");
            paymentSuccessful = true;
            paymentTransaction.setStatus(TransactionStatus.SUCCESSFUL);
            paymentTransaction.setStatusMessage("Successful");
            entityManager.merge(paymentTransaction);

            hostelApplication.setPaymentStatus(TranscriptRequestStatus.PAID);
            entityManager.merge(hostelApplication);

            facesMessages.add("Payment was successful. Thank you for applying for your transcript online.");
            facesMessages.add("Your transcript request has been saved, you can check your request status with your Student Number(" +
                                hostelApplication.getStudentNumber() + ") and Request Number(" + hostelApplication.getRequestNumber() + ")");


        } else if(this.getInterswitchResponseCode().equalsIgnoreCase("00") &&
                this.getInterswitchResponseAmount()!=this.getTotalPaymentAmount()) {

            log.info(":::::::: Payment NOT Successful");
            paymentSuccessful = false;
            this.setInterswitchResponseMessage("Sorry, Transaction was not successful. The amount paid is incorrect");
            paymentTransaction.setStatus(TransactionStatus.FAILED);
            paymentTransaction.setStatusMessage(this.getInterswitchResponseCode() + ":" +
                    this.getInterswitchResponseMessage());
            entityManager.merge(paymentTransaction);
            facesMessages.add(Severity.ERROR, "Sorry, Transaction was not successful. The amount paid is incorrect.");
            facesMessages.add("Your transcript request has been saved, you can check your request status with your Student Number(" +
                                hostelApplication.getStudentNumber() + ") and Request Number(" + hostelApplication.getRequestNumber() + ")");

        } else {
            log.info(":::::::: Payment NOT Successful");
            paymentSuccessful = false;
            this.setInterswitchResponseMessage(paymentService.getInterswitchResponseCodeMessage(getInterswitchResponseCode()));
            paymentTransaction.setStatus(TransactionStatus.FAILED);
            paymentTransaction.setStatusMessage(this.getInterswitchResponseCode() + ":" +
                    this.getInterswitchResponseMessage());
            entityManager.merge(paymentTransaction);
            facesMessages.add(Severity.ERROR, "Sorry, Transaction was not successful Try Again.");
            facesMessages.add("Your transcript request has been saved, you can check your request status with your Student Number(" +
                                hostelApplication.getStudentNumber() + ") and Request Number(" + hostelApplication.getRequestNumber() + ")");

        }
         */
    }
    
    public String cancel() {    	
    	log.info("PaymentRequestAction.cancel() called");
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "index";
    }
        
    public String close() {
    	log.info("PaymentRequestAction.close() called");
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "index";
    }
        
    public void processPinPayment() {
    	log.info("pinNumber = " + pinNumber + " , application No: " + this.getPayeeIdentifier());
    	paymentSuccessful = false;
        EntityTransaction transaction = entityManager.getTransaction();
    	try {
    	Pin pin = paymentService.getPinByPinNumber(pinNumber);
    	if(pin == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Pin Number doesnt exist!",""));
    		
    	} else {
            if(pin.isUsedStatus()) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Pin Number has been used!",""));

            } else {
                if(pin.getPinValue() == totalPaymentAmount) {
                    Calendar calendar = Calendar.getInstance();                    
                    transaction.begin();
                    
                    PaymentTransaction paymentTxn = new PaymentTransaction();
                    paymentTxn.setAmount(totalPaymentAmount);
                    paymentTxn.setDescription(this.getPaymentItemName());
                    paymentTxn.setPaymentDate(calendar.getTime());
                    paymentTxn.setReferenceId(pin.getPinNumber());
                    paymentTxn.setStatus(TransactionStatus.SUCCESSFUL);
                    paymentTxn.setStatusMessage("Payment Successful");
                    paymentTxn.setTransactionId(pin.getPinNumber());
                    paymentTxn.setTransactionType(this.getTransactionType());
                    entityManager.persist(paymentTxn);

                    pin.setUsedStatus(true);
                    pin.setDateUsed(calendar.getTime());
                    entityManager.merge(pin);
                    
                    //update hostel application
                    this.updateHostelApplication(paymentTxn);                    
                    transaction.commit();
                                        
                    paymentSuccessful = true;
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Payment was successful. Thank you for applying for your hostel accommodation.",""));
                    //FacesContext.getCurrentInstance().addMessage(null,
                    //    new FacesMessage(FacesMessage.SEVERITY_INFO, "Your application has been saved, you can check your application status with your Student Number(" +
                    //                            hostelApplication.getStudentNumber() + ") and Application Number(" + hostelApplication.getApplicationNumber() + ")",""));
                    
                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Pin Value ("+pin.getPinValue()+") does not match the corresponding item.",""));

                }
            }
    	}
        } catch(Exception e) {
            log.error("processPinPayment Error: " + e.getMessage(), e);
            transaction.rollback();
        }
    }
    
    public void updateHostelApplication(PaymentTransaction paymentTxn) {
    	
    	Query query = entityManager.createNamedQuery("getHostelApplicationByApplicationNumber"); 
    	query.setParameter(1, this.getPayeeIdentifier());
    	List results = query.getResultList();
    	if(results.size() > 0) {
    		hostelApplication = (HostelApplication)results.get(0);
    	}
    	if(hostelApplication != null) {                
    		hostelApplication.setPaymentStatus(HostelApplicationStatus.PAID);
    		hostelApplication.setPaymentTransactionId(paymentTxn.getId());
    		entityManager.merge(hostelApplication);                
    	} else {
            log.warn(this.getPayeeIdentifier() + " application not found for payment");
        }
    }

	public void setPaymentService(PaymentService paymentService) {
		this.paymentService = paymentService;
	}

	public PaymentService getPaymentService() {
		return paymentService;
	}

	public void setTransactionTypes(List<TransactionType> transactionTypes) {
		this.transactionTypes = transactionTypes;
	}

	public List<TransactionType> getTransactionTypes() {
		return paymentService.getTransactionTypes();
	}
	
	public void setTransactionType(TransactionType transactionType) {
		this.transactionType = transactionType;
	}

	public TransactionType getTransactionType() {
		return transactionType;
	}


	public void setPaymentItems(List<PaymentItem> paymentItems) {
		this.paymentItems = paymentItems;
	}

	public List<PaymentItem> getPaymentItems() {
		return paymentItems;
	}

	public void setInterswitchPayment(boolean interswitchPayment) {
		this.interswitchPayment = interswitchPayment;
	}

	public boolean isInterswitchPayment() {
		return interswitchPayment;
	}

	public void setEtranzactPayment(boolean etranzactPayment) {
		this.etranzactPayment = etranzactPayment;
	}

	public boolean isEtranzactPayment() {
		return etranzactPayment;
	}

	public void setPinPayment(boolean pinPayment) {
		this.pinPayment = pinPayment;
	}

	public boolean isPinPayment() {
		return pinPayment;
	}

	public void setPaymentTransaction(PaymentTransaction paymentTransaction) {
		this.paymentTransaction = paymentTransaction;
	}

	public PaymentTransaction getPaymentTransaction() {
		return paymentTransaction;
	}

	public void setInterswitchMertId(String interswitchMertId) {
		this.interswitchMertId = interswitchMertId;
	}

	public void setPayeeIdentifier(String payeeIdentifier) {
		this.payeeIdentifier = payeeIdentifier;
	}

	public String getPayeeIdentifier() {
		return payeeIdentifier;
	}

	public void setTotalPaymentAmount(double totalPaymentAmount) {
		this.totalPaymentAmount = totalPaymentAmount;
	}

	public double getTotalPaymentAmount() {
		return totalPaymentAmount;
	}

	public String getInterswitchMertId() {
		return interswitchMertId;
	}
	
	public void setInterswitchCadpId(String interswitchCadpId) {
		this.interswitchCadpId = interswitchCadpId;
	}

	public String getInterswitchCadpId() {
		return interswitchCadpId;
	}

	public void setInterswitchTranxRef(String interswitchTranxRef) {
		this.interswitchTranxRef = interswitchTranxRef;
	}

	public String getInterswitchTranxRef() {
		return interswitchTranxRef;
	}

    public String getInterswitchResponse() {
        return interswitchResponse;
    }

    public String getInterswitchResponseCode() {
        return interswitchResponseCode;
    }

    public void setInterswitchResponseCode(String interswitchResponseCode) {
        this.interswitchResponseCode = interswitchResponseCode;
    }

    public double getInterswitchResponseAmount() {
        return interswitchResponseAmount;
    }

    public void setInterswitchResponseAmount(double interswitchResponseAmount) {
        this.interswitchResponseAmount = interswitchResponseAmount;
    }
   
    public void setInterswitchResponse(String interswitchResponse) {
        this.interswitchResponse = interswitchResponse;
    }

    public String getInterswitchResponseMessage() {
        return interswitchResponseMessage;
    }

    public void setInterswitchResponseMessage(String interswitchResponseMessage) {
        this.interswitchResponseMessage = interswitchResponseMessage;
    }

    public void setInterswitchPaymentUrl(String interswitchPaymentUrl) {
            this.interswitchPaymentUrl = interswitchPaymentUrl;
    }

    public String getInterswitchPaymentUrl() {
            return interswitchPaymentUrl;
    }

    public void setTransactionCost(double transactionCost) {
            this.transactionCost = transactionCost;
    }

    public double getTransactionCost() {
            return transactionCost;
    }

    public void setTotalTransactionAmount(double totalTransactionAmount) {
            this.totalTransactionAmount = totalTransactionAmount;
    }

    public double getTotalTransactionAmount() {
            return totalTransactionAmount;
    }

    public void setPaymentItemName(String paymentItemName) {
            this.paymentItemName = paymentItemName;
    }

    public String getPaymentItemName() {
            return paymentItemName;
    }

    public void setPinNumber(String pinNumber) {
            this.pinNumber = pinNumber;
    }

    public String getPinNumber() {
            return pinNumber;
    }

    public void setPaymentSuccessful(boolean paymentSuccessful) {
            this.paymentSuccessful = paymentSuccessful;
    }

    public boolean isPaymentSuccessful() {
        if(hostelApplication == null) {
           return paymentSuccessful;
        } else {
            if(hostelApplication.getPaymentStatus() == HostelApplicationStatus.PAID) {
                    paymentSuccessful = true;
            } else {
                    paymentSuccessful = false;
            }
        }

        return paymentSuccessful;

    }

    public long getTransactionTypeId() {
        return transactionTypeId;
    }

    public void setTransactionTypeId(long transactionTypeId) {
        this.transactionTypeId = transactionTypeId;
    }
    
    @PreDestroy
    public void destroyBean() {
        if(entityManager.isOpen())
            entityManager.close();
        entityManager = null;        
    }
}
