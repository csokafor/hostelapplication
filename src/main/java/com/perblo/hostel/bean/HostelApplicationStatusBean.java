package com.perblo.hostel.bean;

import com.perblo.hostel.entity.EligibleStudent;
import com.perblo.hostel.entity.HostelAllocation;

import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.perblo.hostel.entity.HostelApplication;
import com.perblo.hostel.helper.HostelApplicationHelper;
import com.perblo.hostel.helper.HostelApplicationStatus;
import com.perblo.hostel.helper.HostelRoomAllocationHelper;
import com.perblo.hostel.helper.HostelSettingsHelper;
import com.perblo.hostel.listener.HostelEntityManagerListener;
import com.perblo.payment.PaymentHelper;
import com.perblo.payment.entity.PaymentTransaction;
import com.perblo.payment.entity.Pin;
import java.io.ByteArrayInputStream;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.UploadedFile;


@ManagedBean(name="applicationStatusBean")
@SessionScoped
public class HostelApplicationStatusBean implements Serializable {

    private static final Logger log = Logger.getLogger(HostelApplicationStatusBean.class);
        
    private EntityManager entityManager;
    
    @ManagedProperty(value="#{hostelApplicationHelper}")
    private HostelApplicationHelper hostelApplicationHelper;
         
    @ManagedProperty(value="#{hostelSettingsHelper}")
    private HostelSettingsHelper hostelSettingsHelper;
                
    @ManagedProperty(value="#{hostelRoomAllocationHelper}")
    private HostelRoomAllocationHelper hostelRoomAllocationHelper;  
    
    @ManagedProperty(value="#{paymentHelper}")
    private PaymentHelper paymentHelper;
    
    //@ManagedProperty(value="#{hostelDAO}")
    //private HostelDAO hostelDAO;
        
    private HostelApplication hostelApplication;
    private HostelAllocation hostelAllocation;
    private boolean loggedIn; 
        
    private String applicationNumber;    
    private String studentNumber;    
    private String applicationStatus;    
    private String paymentStatus;
    private String ballotStatus;
    private boolean hasPaid;
    private boolean agreedToRegulations;
    private long facultyId;
    private long departmentId;
    private long cosId;
    
    private String applicationDate;
    private Random random = new Random();
    private boolean hasApplicationEnded;   
    private String hostelApplicationEndDate;
    private String barCodeApplicationNumber = "15000";
        
    //@In(required=false) 
    private String passportFileExtension;
    //@In(required=false) 
    private String passportFileName; 
    private UploadedFile file;
    private DefaultStreamedContent passport;
    
    public HostelApplicationStatusBean() {
    	this.entityManager = HostelEntityManagerListener.createEntityManager();
        //log.info("entityManager: " + this.entityManager.toString());    
        
        hostelApplication = new HostelApplication();
    }   
    
    public String checkHostelApplication() {
        try {
            //check hostel application getHostelApplicationByStudentNumber
            hostelApplication = getHostelApplicationByStudentNumber(studentNumber);

            if (hostelApplication == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Your Student Number is invalid!",""));                
            } else {
                if (hostelApplication.getStudentType().getStudentType().equalsIgnoreCase("Scholarship")) {
                    //check new pin
                    Pin pin = this.getPinByPinNumber(applicationNumber);
                    if (pin == null) {
                        log.warn(studentNumber + " entered invalid pin");
                        FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Your entered an invalid pin!",""));                        
                    } else {
                        if (pin.isUsedStatus()) {
                            log.info(studentNumber + " pin is used");
                            PaymentTransaction paymentTx = paymentHelper.getPaymentTransactionByTransactionId(applicationNumber);
                            HostelApplication studentHostelApp = getHostelApplicationByPaymentTransactionId(paymentTx.getId());
                            if(studentHostelApp == null) {
                                loggedIn = false;
                                FacesContext.getCurrentInstance().addMessage(null,
                                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Pin has been used!",""));                                
                            } else if (studentHostelApp.getStudentNumber().equalsIgnoreCase(studentNumber)) {
                                loggedIn = true;
                            }

                        } else {
                            log.info(studentNumber + " pin is not used");
                            boolean paymentSuccessful = paymentHelper.processPinPayment(hostelApplication, applicationNumber, "Accommodation Fee", hostelApplication.getTotalAmount());
                            if (paymentSuccessful) {
                                loggedIn = true;
                            }
                        }
                    }
                } else {
                    log.info("checkHostelApplication " + studentNumber);
                    hostelApplication = getHostelApplicationByStudentNumberAndApplicationNumber(studentNumber, applicationNumber);

                    if (hostelApplication == null) {
                        loggedIn = false;
                        FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Your Student Number or Application Number is invalid!",""));                            
                    } else {
                        loggedIn = true;
                    }
                    if(this.getHostelApplication().getHostelAllocation() == null) {
                        FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "You have not been assigned hostel accommodation!",""));
                        loggedIn = false;
                    } else {
                        this.setHostelAllocation(this.getHostelApplication().getHostelAllocation());
                        this.setBarCodeApplicationNumber(hostelApplication.getApplicationNumber().substring(1));
                    }
                  
                }

            }
            
            
            //check hostel ballot end date
            Date now = new Date();
            Date applicationEndDate = hostelSettingsHelper.getHostelApplicationEndDate();
            if(now.after(applicationEndDate)) {
                hasApplicationEnded = true;
                log.info("application ended");
            } else {
                hasApplicationEnded = false;
                log.info("appliction has not ended");
            }
            

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot check your Hostel application!",""));            
            log.error(e.getMessage(), e);            
        }
        if(loggedIn) {
            return "hostelregulations";
        } else {
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            return "checkapplicationstatus";
        }
        
    }
    
    public String confirmPayment() {
        boolean hasPaidSchoolFees = false;
                
        EligibleStudent eligibleStudent = hostelApplicationHelper.getEligibleStudentByStudentNumber(hostelApplication.getStudentNumber());
        if(eligibleStudent == null) {
            hasPaidSchoolFees = false;
        } else {
            if (eligibleStudent.getHostelStudentType().getStudentType().equalsIgnoreCase("Scholarship")) {
                hasPaidSchoolFees = false;
                log.info(hostelApplication.getStudentNumber() + " is a scholarship student");
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Scholarship students do not pay accommodation fee. "
                        + "Use your matric number and hostel pin to check your application status and complete your application.",""));             
                                            
            } else {
                hasPaidSchoolFees = true;
            }            
        }
        if(hasPaidSchoolFees) {
            return "paymentconfirmation";
        } else {
            return "updateapplicationstatus";
        }
        
    }
    
    public void doAccommodationBallot() {
        log.info("HostelApplicationStatusAction.doAccommodationBallot() called");
        try {
            int ballotNo = random.nextInt(5);
            if(ballotNo < 2) {
                log.info("ballotNo = " + ballotNo + ". Ballot succesful");                                
              
                hostelAllocation = hostelRoomAllocationHelper.getHosteRoomAllocation(hostelApplication);   
                if(hostelAllocation == null) {
                    log.info("hostelAllocation is null");                    
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Sorry a room could not be allocated to you.",""));  
                    hostelApplication.setBallotStatus(HostelApplicationStatus.UNSUCCESSFUL);
                } else {
                    hostelApplication.setBallotStatus(HostelApplicationStatus.SUCCESSFUL);
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Congratulations you won the ballot for accommodation. Please pay for your accommodation","")); 
                    
                    hostelAllocation.setAcademicSession(hostelSettingsHelper.getCurrentAcademicSession());
                    entityManager.persist(hostelAllocation);             
                    hostelApplication.setHostelAllocation(hostelAllocation);
                }
                
            } else {
                log.info("ballotNo = " + ballotNo + ". Ballot not succesful");
                hostelApplication.setBallotStatus(HostelApplicationStatus.UNSUCCESSFUL);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sorry you did not win ballot for accommodation.",""));  
            }
            if(hostelApplication.getId() == null) {
                entityManager.persist(hostelApplication);
            } else {
                entityManager.merge(hostelApplication);
            }
            
            
        } catch(Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ballot for accommodation failed!",""));            
            log.error(e.getMessage(), e);
        }
    }    
    
    public void allocateHostelForHandicapStudent() {
        log.info("allocateHostelForHandicapStudent called");
        try {
            if (hostelApplication.getStudentType().getStudentType().equalsIgnoreCase("Handicap")) {
                
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "You are eligible for hostel accommodation!",""));
                hostelAllocation = hostelRoomAllocationHelper.getHosteRoomAllocation(hostelApplication);
                if (hostelAllocation == null) {
                    log.info("hostelAllocation is null");
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Sorry a room could not be allocated to you",""));                    
                    hostelApplication.setBallotStatus(HostelApplicationStatus.UNSUCCESSFUL);

                } else {
                    hostelApplication.setBallotStatus(HostelApplicationStatus.SUCCESSFUL);
                    hostelAllocation.setAcademicSession(hostelSettingsHelper.getCurrentAcademicSession());
                    entityManager.persist(hostelAllocation);
                    hostelApplication.setHostelAllocation(hostelAllocation);
                }
                entityManager.merge(hostelApplication);

            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "This feature is only for handicap students!",""));                 
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Allocation of Hostel failed!",""));            
            log.error(e.getMessage(), e);
        }
    }

    public UploadedFile getFile() {
        return file;
    }
 
    public void setFile(UploadedFile file) {
        this.file = file;
    }
     
    public void upload() {
        if(file != null) {
            //FacesMessage message = new FacesMessage("Succesful", file.getFileName() + " is uploaded.");
            //FacesContext.getCurrentInstance().addMessage(null, message);
            
            if(file.getContentType().contains("jpg") || file.getContentType().contains("jpeg")) {
                                
                EntityTransaction transaction = entityManager.getTransaction();
                transaction.begin();
                log.info("file content size: " + file.getContents().length);
                log.info("content type: " + file.getContentType());
                log.info("file name: " + file.getFileName());
                
                hostelApplication.setPassportData(file.getContents());
                hostelApplication.setPassportContentType(file.getContentType());
                entityManager.merge(hostelApplication);
                transaction.commit();
                
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Passport has been uploaded.",""));                
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Please upload a jpg/jpeg file.",""));                 
            }                     
            
            
        }
    }

    public DefaultStreamedContent getPassport() {
        if(hostelApplication.getPassportData() != null) {
            passport = new DefaultStreamedContent(
                    new ByteArrayInputStream(hostelApplication.getPassportData()), 
                    hostelApplication.getPassportContentType());
        }
        return passport;
    }

    public void setPassport(DefaultStreamedContent passport) {
        this.passport = passport;
    }
    
    
   
    /*
    public void uploadPassport() {
        try {
            log.info("uploadPassport: passportContentType - " + hostelApplication.getPassportContentType());
            
            if(getPassportFileName().contains("jpg") || getPassportFileName().contains("jpeg")) {
                entityManager.merge(hostelApplication);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Passport has been uploaded.",""));                
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Please upload a jpg/jpeg file.",""));                 
            }           
    	    
            
        } catch(Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Passport upload failed!",""));            
            log.error(e.getMessage(), e);
        }            	
    }
    */
    
    public String printApplicationStatus() {
        log.info("printApplicationStatus");
        return "printapplicationstatus";
    }
    
    public String acceptHostelRegulations() {
        log.info("acceptHostelRegulations: " + agreedToRegulations);
        if(!agreedToRegulations) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "You must accept Hostel Rules and Regulations!",""));            
        }
        if(agreedToRegulations) {
            return "updateapplicationstatus";
        } else {
            return "hostelregulations";
        }
       
    }

    //@End
    public String cancel() {
        log.info("HostelApplicationStatusAction.cancel() called");
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();        
        return "index";
    }

    //@End
    public String close() {
        log.info("HostelApplicationStatusAction.close() called");
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();        
        return "index";
    }
    
    private Pin getPinByPinNumber(String pinNumber) {
        try {
            Query query = entityManager.createNamedQuery("getPinByPinNumber");
            query.setParameter(1, pinNumber);

            List resultList = query.getResultList();
            if(resultList.size() > 0) {
                    Pin pin = (Pin)resultList.get(0);
                    return pin;
            } else {
                    return null;
            }
        } catch(Exception e) {
            log.error("Exception in getPinByPinNumber: " + e.getMessage());
            return null;
        }
    	
    }
    
    private HostelApplication getHostelApplicationByStudentNumberAndApplicationNumber(String studentNumber, String applicationNumber) {
        HostelApplication hostelApp = null;
        try {
            Query queryObj = entityManager.createNamedQuery("getHostelApplicationByStudentNumberAndApplicationNumber");
            queryObj.setParameter(1, studentNumber);
            queryObj.setParameter(2, applicationNumber);
            List<HostelApplication> results = queryObj.getResultList();

            if (results.isEmpty()) {                
                hostelApp = null;
            } else {
                hostelApp = (HostelApplication) results.get(0);                
            }
            
        } catch(Exception e) {
            log.error("Exception in getHostelApplicationByStudentNumberAndApplicationNumber: " + e.getMessage());            
        }
        
        return hostelApp;
    }
    
    private HostelApplication getHostelApplicationByStudentNumber(String studentNumber) {
        HostelApplication hostelApp = null;
        try {
            Query queryObj = entityManager.createNamedQuery("getHostelApplicationByStudentNumber");
            queryObj.setParameter(1, studentNumber);            
            List<HostelApplication> results = queryObj.getResultList();

            if (results.isEmpty()) {                
                hostelApp = null;
            } else {
                hostelApp = (HostelApplication) results.get(0);                
            }
            
        } catch(Exception e) {
            log.error("Exception in getHostelApplicationByStudentNumberAndApplicationNumber: " + e.getMessage());            
        }
        
        return hostelApp;
    }
    
    private HostelApplication getHostelApplicationByPaymentTransactionId(long paymentTransactionId) {
        HostelApplication hostelApp = null;
        try {
            Query queryObj = entityManager.createNamedQuery("getHostelApplicationByPaymentTransactionId");
            queryObj.setParameter(1, paymentTransactionId);            
            List<HostelApplication> results = queryObj.getResultList();

            if (results.isEmpty()) {                
                hostelApp = null;
            } else {
                hostelApp = (HostelApplication) results.get(0);                
            }
            
        } catch(Exception e) {
            log.error("Exception in getHostelApplicationByPaymentTransactionId: " + e.getMessage());            
        }
        
        return hostelApp;
    }
      
    

    public String getApplicationStatus() {
        return HostelApplicationStatus.getApplicationStatus(hostelApplication.getApplicationStatus());
    }

    public String getPaymentStatus() {
        return HostelApplicationStatus.getPaymentStatus(hostelApplication.getPaymentStatus());
    }

    public String getBallotStatus() {
        return HostelApplicationStatus.getBallotStatus(hostelApplication.getBallotStatus());
    }
    
    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public HostelAllocation getHostelAllocation() {
        return hostelAllocation;
    }

    public void setHostelAllocation(HostelAllocation hostelAllocation) {
        this.hostelAllocation = hostelAllocation;
    }

    public String getBarCodeApplicationNumber() {
        return barCodeApplicationNumber;
    }

    public void setBarCodeApplicationNumber(String barCodeApplicationNumber) {
        this.barCodeApplicationNumber = barCodeApplicationNumber;
    }
        
    public void setHostelApplication(HostelApplication hostelApplication) {
        this.hostelApplication = hostelApplication;
    }

    public HostelApplication getHostelApplication() {
        return hostelApplication;
    }

    public void setApplicationStatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public void setHasPaid(boolean hasPaid) {
        this.hasPaid = hasPaid;
    }
    

    public String getApplicationDate() {
        String requestDateString = new java.text.SimpleDateFormat("dd-MMM-yyyy").format(hostelApplication.getApplicationDate());
        return requestDateString;
    }

    public void setApplicationDate(String applicationDate) {
        this.applicationDate = applicationDate;
    }

    public boolean isHasPaid() {
        if (hostelApplication == null) {
            return true;
        } else {
            if (hostelApplication.getPaymentStatus() == HostelApplicationStatus.PAID) {
                return true;
            } else {
                return false;
            }
        }
    }

    public String getPassportFileExtension() {
        return passportFileExtension;
    }

    public void setPassportFileExtension(String passportFileExtension) {
        this.passportFileExtension = passportFileExtension;
    }

    public String getPassportFileName() {
        return passportFileName;
    }

    public void setPassportFileName(String passportFileName) {
        this.passportFileName = passportFileName;
    }

    public boolean isAgreedToRegulations() {
        return agreedToRegulations;
    }

    public void setAgreedToRegulations(boolean agreedToRegulations) {
        this.agreedToRegulations = agreedToRegulations;
    }
		
    public boolean isHasApplicationEnded() {
        return hasApplicationEnded;
    }

    public void setHasApplicationEnded(boolean hasApplicationEnded) {
        this.hasApplicationEnded = hasApplicationEnded;
    }
        
    
    public String getHostelApplicationEndDate() {
     if(hostelApplicationEndDate == null) {            
        hostelApplicationEndDate = SimpleDateFormat.getDateTimeInstance(
            DateFormat.MEDIUM, DateFormat.SHORT).format(hostelSettingsHelper.getHostelBallotEndDate());
    }
    return hostelApplicationEndDate;
    }

    public void setHostelApplicationEndDate(String hostelApplicationEndDate) {
        this.hostelApplicationEndDate = hostelApplicationEndDate;
    }

    public long getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(long facultyId) {
        this.facultyId = facultyId;
    }

    public long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(long departmentId) {
        this.departmentId = departmentId;
    }

    public long getCosId() {
        return cosId;
    }

    public void setCosId(long cosId) {
        this.cosId = cosId;
    }

    public HostelApplicationHelper getHostelApplicationHelper() {
        return hostelApplicationHelper;
    }

    public void setHostelApplicationHelper(HostelApplicationHelper hostelApplicationHelper) {
        this.hostelApplicationHelper = hostelApplicationHelper;
    }

    public HostelSettingsHelper getHostelSettingsHelper() {
        return hostelSettingsHelper;
    }

    public void setHostelSettingsHelper(HostelSettingsHelper hostelSettingsHelper) {
        this.hostelSettingsHelper = hostelSettingsHelper;
    }

    public HostelRoomAllocationHelper getHostelRoomAllocationHelper() {
        return hostelRoomAllocationHelper;
    }

    public void setHostelRoomAllocationHelper(HostelRoomAllocationHelper hostelRoomAllocationHelper) {
        this.hostelRoomAllocationHelper = hostelRoomAllocationHelper;
    }

    public PaymentHelper getPaymentHelper() {
        return paymentHelper;
    }

    public void setPaymentHelper(PaymentHelper paymentHelper) {
        this.paymentHelper = paymentHelper;
    }

    /*
    public HostelDAO getHostelDAO() {
        return hostelDAO;
    }

    public void setHostelDAO(HostelDAO hostelDAO) {
        this.hostelDAO = hostelDAO;
    }
    */
    
    @PreDestroy
    public void destroyBean() {
        if(entityManager.isOpen())
            entityManager.close();
        entityManager = null;       
    }
    
}