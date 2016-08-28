package com.perblo.payment;

import com.perblo.hostel.bean.HostelDAO;
import com.perblo.hostel.entity.HostelApplication;
import com.perblo.hostel.helper.HostelApplicationStatus;
import com.perblo.hostel.listener.HostelEntityManagerListener;
import com.perblo.payment.constants.TransactionStatus;
import java.text.DecimalFormat;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.perblo.payment.entity.PaymentItem;
import com.perblo.payment.entity.PaymentSetting;
import com.perblo.payment.entity.PaymentTransaction;
import com.perblo.payment.entity.Pin;
import com.perblo.payment.entity.TransactionType;
import java.io.Serializable;
import java.util.Calendar;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import org.apache.log4j.Logger;

@ManagedBean(name="paymentHelper")
@SessionScoped
public class PaymentHelper implements Serializable {
	
    public static final String INTERSWITCH_MERTID = "InterswitchMertId";
    public static final String INTERSWITCH_PAYMENTITEMNAME = "InterswitchPayItemName";	
    public static final String INTERSWITCH_CADPID = "InterswitchCadpId";	
    public static final String INTERSWITCH_PAYMENTURL = "InterswitchPaymentUrl";    
    public static final String INTERSWITCH_RESPONSEURL = "InterSwitchResponseUrl";

    private static final Logger log = Logger.getLogger(PaymentHelper.class);    
    private EntityManager entityManager;
            
    private List<TransactionType> transactionTypes =  null;
    
    public PaymentHelper() {
    	this.entityManager = HostelEntityManagerListener.createEntityManager();
        //log.info("entityManager: " + this.entityManager.toString());        
    }          
    
    public String getPaymentSettingByName(String settingName) {
    	Query query = entityManager.createNamedQuery("getPaymentSettingByName");
    	query.setParameter(1, settingName);    	
    	List resultList = query.getResultList();    	
    	if(resultList.size() > 0) {
    		PaymentSetting paymentSetting = (PaymentSetting)resultList.get(0);
        	return paymentSetting.getValue();
    	} else {
    		return "";
    	}
    	
    }
            
    public PaymentItem getPaymentItemByNameAndPolicy(String paymentItemName, Long policyId) {
    	Query query = entityManager.createNamedQuery("getPaymentItemByNameAndPolicy");
    	query.setParameter(1, paymentItemName);
    	query.setParameter(2, policyId);
    	List resultList = query.getResultList();
    	if(resultList.size() > 0) {
    		PaymentItem paymentItem = (PaymentItem)resultList.get(0);
        	return paymentItem;
    	} else {
    		return null;
    	}
    	
    }
    
    public Pin getPinByPinNumber(String pinNumber) {
    	Query query = entityManager.createNamedQuery("getPinByPinNumber");
    	query.setParameter(1, pinNumber);
    	
    	List resultList = query.getResultList();
    	if(resultList.size() > 0) {
    		Pin pin = (Pin)resultList.get(0);
        	return pin;
    	} else {
    		return null;
    	}
    	
    }
    
    public boolean processPinPayment(HostelApplication hostelApplication, String pinNumber, String paymentItem, double amount) {
        log.info("processPinPayment pinNumber = " + pinNumber);
        boolean paymentSuccessful = false;
        try {
            Pin pin = getPinByPinNumber(pinNumber);
            if (pin == null) {
                log.warn("Pin Number doesnt exist!");
            } else {
                if (pin.isUsedStatus()) {
                    log.warn("Pin Number has been used!");
                } else {
                    if (pin.getPinValue() == amount) {
                        Calendar calendar = Calendar.getInstance();
                        EntityTransaction transaction = entityManager.getTransaction();
                        transaction.begin();
                        PaymentTransaction paymentTxn = new PaymentTransaction();
                        paymentTxn.setAmount(amount);
                        paymentTxn.setDescription(paymentItem);
                        paymentTxn.setPaymentDate(calendar.getTime());
                        paymentTxn.setReferenceId(pin.getPinNumber());
                        paymentTxn.setStatus(TransactionStatus.SUCCESSFUL);
                        paymentTxn.setStatusMessage("Payment Successful");
                        paymentTxn.setTransactionId(pin.getPinNumber());
                        paymentTxn.setTransactionType(this.getTransactionTypes().get(0));
                        entityManager.persist(paymentTxn);

                        pin.setUsedStatus(true);
                        pin.setDateUsed(calendar.getTime());
                        entityManager.merge(pin);

                        //update hostel application
                        hostelApplication.setPaymentStatus(HostelApplicationStatus.PAID);
                        hostelApplication.setPaymentTransactionId(paymentTxn.getId());
                        entityManager.merge(hostelApplication);
                        transaction.commit();
                        
                        paymentSuccessful = true;
                        log.info("Payment successful");
                    } else {
                        log.warn("Pin Value (" + pin.getPinValue() + ") does not match the corresponding item.");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception in processPinPayment: " + e.getMessage());
        }
        
        return paymentSuccessful;
    }
    
    public PaymentTransaction getPaymentTransactionByTransactionId(String transactionId) {
        PaymentTransaction paymentTx = null;
        try {
            Query query = entityManager.createNamedQuery("getPaymentTransactionByTransactionId");
            query.setParameter(1, transactionId);

            List resultList = query.getResultList();
            if(resultList.size() > 0) {
                paymentTx = (PaymentTransaction)resultList.get(0);                    
            } 
            
        } catch(Exception e) {
            log.error("Exception in getPaymentTransactionByTransactionId: " + e.getMessage());
        }
        
        return paymentTx;
    }
    
            
    public List<TransactionType> getTransactionTypes() {
    	Query query = entityManager.createNamedQuery("getEnabledTransactionTypes"); 
    	transactionTypes = query.getResultList();
		return transactionTypes;
    }

    public void setTransactionTypes(List<TransactionType> transactionTypes) {
            this.transactionTypes = transactionTypes;
    }
        
    public static String doubleToString(double number, String numberFormat)  {
    DecimalFormat decimalFormat = new DecimalFormat(numberFormat);
    return decimalFormat.format(number);
    }

    public String getInterswitchResponseCodeMessage(String responseCode) {
        String message = "";
        if (responseCode.equalsIgnoreCase("00")) {
            message = "Your Payment completed Successfully.";
        } else if (responseCode.equalsIgnoreCase("01")) {
            message = "Please Refer to card issuer.";
        } else if (responseCode.equalsIgnoreCase("02")) {
            message = "Please Refer to card issuer, special condition.";
        } else if (responseCode.equalsIgnoreCase("03")) {
            message = "Please Refer to card issuer.";
        } else if (responseCode.equalsIgnoreCase("04")) {
            message = "Pick-up card.";
        } else if (responseCode.equalsIgnoreCase("05")) {
            message = "Do Not honor.";
        } else if (responseCode.equalsIgnoreCase("06")) {
            message = "ERROR.";
        } else if (responseCode.equalsIgnoreCase("07")) {
            message = "Pick-up card, special condition.";
        } else if (responseCode.equalsIgnoreCase("08")) {
            message = "Honor with identification.";
        } else if (responseCode.equalsIgnoreCase("09")) {
            message = "Request in progress.";
        } else if (responseCode.equalsIgnoreCase("10")) {
            message = "Approved, partial.";
        } else if (responseCode.equalsIgnoreCase("11")) {
            message = "Approved, VIP.";
        } else if (responseCode.equalsIgnoreCase("12")) {
            message = "Invalid transaction.";
        } else if (responseCode.equalsIgnoreCase("13")) {
            message = "Invalid amount.";
        } else if (responseCode.equalsIgnoreCase("14")) {
            message = "Invalid card number.";
        } else if (responseCode.equalsIgnoreCase("15")) {
            message = "No such issuer.";
        } else if (responseCode.equalsIgnoreCase("16")) {
            message = "Approved, update track 3.";
        } else if (responseCode.equalsIgnoreCase("17")) {
            message = "Customer cancellation.";
        } else if (responseCode.equalsIgnoreCase("18")) {
            message = "Customer dispute.";
        } else if (responseCode.equalsIgnoreCase("19")) {
            message = "Re-enter transaction.";
        } else if (responseCode.equalsIgnoreCase("20")) {
            message = "Invalid response.";
        } else if (responseCode.equalsIgnoreCase("21")) {
            message = "No action taken.";
        } else if (responseCode.equalsIgnoreCase("22")) {
            message = "Suspected malfunction.";
        } else if (responseCode.equalsIgnoreCase("23")) {
                message = "Suspected malfunction.";
        } else if (responseCode.equalsIgnoreCase("24")) {
                message = "File update not supported.";
        }
        else if (responseCode.equalsIgnoreCase("25"))
        {
                message = "Unable to locate record.";
        }
        else if (responseCode.equalsIgnoreCase("26"))
        {
                message = "Duplicate record.";
        }
        else if (responseCode.equalsIgnoreCase("27"))
        {
                message = "File update field edit error.";
        }
        else if (responseCode.equalsIgnoreCase("28"))
        {
                message = "File update file locked.";
        }
        else if (responseCode.equalsIgnoreCase("29"))
        {
                message = "File update failed.";
        }
        else if (responseCode.equalsIgnoreCase("30"))
        {
                message = "Format error.";
        }
        else if (responseCode.equalsIgnoreCase("31"))
        {
                message = "Bank not supported.";
        }
        else if (responseCode.equalsIgnoreCase("32"))
        {
                message = "Completed partially.";
        }
        else if (responseCode.equalsIgnoreCase("33"))
        {
                message = "Expired card, pick-up.";
        }
        else if (responseCode.equalsIgnoreCase("34"))
        {
                message = "Suspected fraud, pick-up.";
        }
        else if (responseCode.equalsIgnoreCase("35"))
        {
                message = "Contact acquirer, pick-up.";
        }
        else if (responseCode.equalsIgnoreCase("36"))
        {
                message = "Restricted card, pick-up.";
        }
        else if (responseCode.equalsIgnoreCase("37"))
        {
                message = "Call acquirer security, pick-up.";
        }
        else if (responseCode.equalsIgnoreCase("38"))
        {
                message = "PIN tries exceeded, pick-up.";
        }
        else if (responseCode.equalsIgnoreCase("39"))
        {
                message = "No credit account.";
        }
        else if (responseCode.equalsIgnoreCase("40"))
        {
                message = "Function not supported.";
        }
        else if (responseCode.equalsIgnoreCase("41"))
        {
                message = "Lost card, pick-up.";
        }
        else if (responseCode.equalsIgnoreCase("42"))
        {
                message = "No universal account.";
        }
        else if (responseCode.equalsIgnoreCase("43"))
        {
                message = "Stolen card, pick-up.";
        }
        else if (responseCode.equalsIgnoreCase("44"))
        {
                message = "No investment account.";
        }
        else if (responseCode.equalsIgnoreCase("45"))
        {
                message = "Account closed.";
        }
        else if (responseCode.equalsIgnoreCase("46"))
        {
                message = "Identification required.";
        }
        else if (responseCode.equalsIgnoreCase("47"))
        {
                message = "Identification cross-check required.";
        }
        else if (responseCode.equalsIgnoreCase("51"))
        {
                message = "Not sufficient funds.";
        }
        else if (responseCode.equalsIgnoreCase("52"))
        {
                message = "No check account.";
        }
        else if (responseCode.equalsIgnoreCase("53"))
        {
                message = "No savings account.";
        }
        else if (responseCode.equalsIgnoreCase("54"))
        {
                message = "Expired card.";
        }
        else if (responseCode.equalsIgnoreCase("55"))
        {
                message = "Incorrect PIN.";
        }
        else if (responseCode.equalsIgnoreCase("56"))
        {
                message = "No card record.";
        }
        else if (responseCode.equalsIgnoreCase("57"))
        {
                message = "Transaction not permitted to cardholder.";
        }
        else if (responseCode.equalsIgnoreCase("58"))
        {
                message = "Transaction not permitted on terminal.";
        }
        else if (responseCode.equalsIgnoreCase("59"))
        {
                message = "Suspected fraud.";
        }
        else if (responseCode.equalsIgnoreCase("60"))
        {
                message = "Contact acquirer.";
        }
        else if (responseCode.equalsIgnoreCase("61"))
        {
                message = "Exceeds withdrawal limit.";
        }
        else if (responseCode.equalsIgnoreCase("62"))
        {
                message = "Restricted card.";
        }
        else if (responseCode.equalsIgnoreCase("63"))
        {
                message = "Security violation.";
        }
        else if (responseCode.equalsIgnoreCase("64"))
        {
                message = "Original amount incorrect.";
        }
        else if (responseCode.equalsIgnoreCase("65"))
        {
                message = "Exceeds withdrawal frequency.";
        }
        else if (responseCode.equalsIgnoreCase("66"))
        {
                message = "Call acquirer security.";
        }
        else if (responseCode.equalsIgnoreCase("67"))
        {
                message = "Hard capture.";
        }
        else if (responseCode.equalsIgnoreCase("68"))
        {
                message = "Response received too late.";
        }
        else if (responseCode.equalsIgnoreCase("69"))
        {
                message = "Advice received too late.";
        }
        else if (responseCode.equalsIgnoreCase("75"))
        {
                message = "PIN tries exceeded.";
        }
        else if (responseCode.equalsIgnoreCase("76"))
        {
                message = "Reserved for future Postilion use.";
        }
        else if (responseCode.equalsIgnoreCase("77"))
        {
                message = "Intervene, bank approval required.";
        }
        else if (responseCode.equalsIgnoreCase("78"))
        {
                message = "Intervene, bank approval required for partial amount.";
        }
        else if (responseCode.equalsIgnoreCase("90"))
        {
                message = "Cut-off in progress.";
        }
        else if (responseCode.equalsIgnoreCase("91"))
        {
                message = "Issuer or switch inoperative.";
        }
        else if (responseCode.equalsIgnoreCase("92"))
        {
                message = "Routing error.";
        }
        else if (responseCode.equalsIgnoreCase("93"))
        {
                message = "Violation of law.";
        }
        else if (responseCode.equalsIgnoreCase("94"))
        {
                message = "Duplicate transaction.";
        }
        else if (responseCode.equalsIgnoreCase("95"))
        {
                message = "Reconcile error.";
        }
        else if (responseCode.equalsIgnoreCase("96"))
        {
                message = "System malfunction.";
        }
        else if (responseCode.equalsIgnoreCase("97"))
        {
                message = "Reserved for future Postilion use.";
        }
        else if (responseCode.equalsIgnoreCase("98"))
        {
                message = "Exceeds cash limit.";
        }
        else if (responseCode.equalsIgnoreCase("W06"))
        {
                message = "Application Error.";
        }
        else if (responseCode.equalsIgnoreCase("W09"))
        {
                message = "Request In Progress.";
        }
        else if (responseCode.equalsIgnoreCase("W17"))
        {
                message = "Customer Cancellation.";
        }
        else if (responseCode.equalsIgnoreCase("W56"))
        {
                message = "No Transaction Record.";
        }
        else if (responseCode.equalsIgnoreCase("W57"))
        {
                message = "Merchant Deactivation.";
        }
        else if (responseCode.equalsIgnoreCase("W63"))
        {
                message = "Security Violation.";
        }
        else if (responseCode.equalsIgnoreCase("W94"))
        {
                message = "Duplicate Transaction Ref.";
        }
        else if (responseCode.equalsIgnoreCase("X00"))
        {
                message = "Transaction could not be authorized. Please contact your bank or send an email to webpay.support@interswitchng.com";
        }
        else if (responseCode.equalsIgnoreCase("X01"))
        {
                message = "Transaction could not be authorized. Please contact your bank or send an email to webpay.support@interswitchng.com";
        }
        else if (responseCode.equalsIgnoreCase("X02"))
        {
                message = "Transaction could not be authorized. Please contact your bank or send an email to webpay.support@interswitchng.com";
        }
        else if (responseCode.equalsIgnoreCase("X03"))
        {
                message = "Transaction could not be authorized. Please contact your bank or send an email to webpay.support@interswitchng.com";
        }
        else if (responseCode.equalsIgnoreCase("X04"))
        {
                message = "Transaction could not be authorized. Please contact your bank or send an email to webpay.support@interswitchng.com";
        }
        else if (responseCode.equalsIgnoreCase("X05"))
        {
                message = "Transaction could not be authorized. Please contact your bank or send an email to webpay.support@interswitchng.com";
        }

        return message;
    }
    
    @PreDestroy
    public void destroyBean() {
        if(entityManager.isOpen())
            entityManager.close();
        entityManager = null;        
    }
}
