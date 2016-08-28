package com.perblo.hostel.bean;

import com.perblo.hostel.entity.HostelAllocation;

import java.io.Serializable;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import com.perblo.hostel.entity.HostelApplication;
import com.perblo.hostel.entity.HostelBallotApplication;
import com.perblo.hostel.helper.HostelApplicationStatus;
import com.perblo.hostel.helper.HostelRoomAllocationHelper;
import com.perblo.hostel.helper.HostelSettingsHelper;
import com.perblo.hostel.listener.HostelEntityManagerListener;
//import com.perblo.payment.PaymentHelper;
import com.perblo.payment.entity.PaymentTransaction;
import com.perblo.payment.entity.Pin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import javax.annotation.PreDestroy;
import javax.faces.bean.SessionScoped;

import org.apache.log4j.Logger;

@ManagedBean(name="ballotStatusBean")
@SessionScoped
public class HostelBallotApplicationStatusBean implements Serializable {

    private static final Logger log = Logger.getLogger(HostelBallotApplicationStatusBean.class);
    
    private EntityManager entityManager;
                       
    private HostelBallotApplication ballotApplication;    
    private boolean loggedIn;         
    private String applicationNumber;    
    private String studentNumber;
    private int ballotStatus;
    private Random random = new Random();
    
    private String applicationDate = null;
    
    @ManagedProperty(value="#{hostelSettingsHelper}")
    private HostelSettingsHelper hostelSettingsHelper;
            
    //@In(required=false) 
    //private byte[] passportData; 
    //@In(required=false) 
    private String passportFileExtension;
    //@In(required=false) 
    private String passportFileName; 
    //@In(required=false) 
    //private String passportContentType;
    
    private boolean hasBallotEnded;   
    private String hostelBallotEndDate;
    
    public HostelBallotApplicationStatusBean() {
    	this.entityManager = HostelEntityManagerListener.createEntityManager();
        //log.info("entityManager: " + this.entityManager.toString());
    }
          
    public void checkBallotApplication() {
        try {
            ballotApplication = getHostelBallotApplicationByStudentNumber(studentNumber);

            if (ballotApplication == null) {
            	FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Your Student Number is invalid!",""));
            } else {
                
                ballotApplication = getHostelBallotApplicationByStudentNumberAndApplicationNumber(studentNumber, applicationNumber);
                if (ballotApplication == null) {
                    loggedIn = false;
                    FacesContext.getCurrentInstance().addMessage(null,
        		new FacesMessage(FacesMessage.SEVERITY_ERROR, "Your Student Number or Ballot Number is invalid!",""));
                    
                } else {
                    loggedIn = true;
                    ballotStatus = ballotApplication.getBallotStatus();
                    log.info("ballotApplication: " + ballotApplication);
                    //check hostel ballot end date
                    Date now = new Date();
                    Date ballotEndDate = hostelSettingsHelper.getHostelBallotEndDate();
                    if(now.after(ballotEndDate)) {
                        hasBallotEnded = true;
                    } else {
                        hasBallotEnded = false;
                    }
                }                
            }          

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot check your Hostel application!",""));
            log.error(e.getMessage(), e);            
        }
        
    }
           
    public void uploadPassport() {
        try {
            log.info("uploadPassport");
    	    entityManager.merge(ballotApplication);
    	    FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_INFO, "Passport has been uploaded.",""));
            
        } catch(Exception e) {
        	FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Passport upload failed!",""));            
            log.error(e.getMessage());
        }            	
    }
    
    public void printBallotStatus() {
        log.info("printApplicationStatus");
    }
    
    
    //@End
    public void cancel() {
        log.info("HostelBallotApplicationStatusAction.cancel() called");
    }

    //@End
    public String close() {
        log.info("HostelBallotApplicationStatusAction.close() called");
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();        
        return "index";
    }
    
        
    private HostelBallotApplication getHostelBallotApplicationByStudentNumberAndApplicationNumber(String studentNumber, String applicationNumber) {
        HostelBallotApplication hostelApp = null;
        try {
            Query queryObj = entityManager.createNamedQuery("getHostelBallotApplicationByStudentNumberAndApplicationNumber");
            queryObj.setParameter(1, studentNumber);
            queryObj.setParameter(2, applicationNumber);
            List<HostelBallotApplication> results = queryObj.getResultList();

            if (results.isEmpty()) {                
                hostelApp = null;
            } else {
                hostelApp = (HostelBallotApplication) results.get(0);                
            }
            
        } catch(Exception e) {
            log.error("Exception in getHostelBallotApplicationByStudentNumberAndApplicationNumber: " + e.getMessage());            
        }
        
        return hostelApp;
    }
    
    private HostelBallotApplication getHostelBallotApplicationByStudentNumber(String studentNumber) {
        HostelBallotApplication hostelApp = null;
        try {
            Query queryObj = entityManager.createNamedQuery("getHostelBallotApplicationByStudentNumber");
            queryObj.setParameter(1, studentNumber);            
            List<HostelBallotApplication> results = queryObj.getResultList();

            if (results.isEmpty()) {                
                hostelApp = null;
            } else {
                hostelApp = (HostelBallotApplication) results.get(0);                
            }
            
        } catch(Exception e) {
            log.error("Exception in getHostelBallotApplicationByStudentNumberAndApplicationNumber: " + e.getMessage());            
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
    
    public void setBallotStatus(int ballotStatus) {
        this.ballotStatus = ballotStatus;
    }
    
    public int getBallotStatus() {
        return ballotStatus;
    }
          
    public String getBallotStatusString() {
        return HostelApplicationStatus.getBallotStatus(ballotApplication.getBallotStatus());
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

    public HostelSettingsHelper getHostelSettingsHelper() {
        return hostelSettingsHelper;
    }

    public void setHostelSettingsHelper(HostelSettingsHelper hostelSettingsHelper) {
        this.hostelSettingsHelper = hostelSettingsHelper;
    }
   
    public void setBallotApplication(HostelBallotApplication hostelBallotApplication) {
        this.ballotApplication = hostelBallotApplication;
    }

    public HostelBallotApplication getBallotApplication() {
        return ballotApplication;
    }    

    public String getApplicationDate() {
        String requestDateString = new java.text.SimpleDateFormat("dd-MMM-yyyy").format(ballotApplication.getApplicationDate());
        return requestDateString;
    }

    public void setApplicationDate(String applicationDate) {
        this.applicationDate = applicationDate;
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
    
     public boolean getHasBallotEnded() {
        return hasBallotEnded;
    }

    public void setHasBallotEnded(boolean hasBallotEnded) {
        this.hasBallotEnded = hasBallotEnded;
    }

    public String getHostelBallotEndDate() {
        if(hostelBallotEndDate == null) {            
            hostelBallotEndDate = SimpleDateFormat.getDateTimeInstance(
                DateFormat.MEDIUM, DateFormat.SHORT).format(hostelSettingsHelper.getHostelBallotEndDate());
        }
        return hostelBallotEndDate;
    }

    public void setHostelBallotEndDate(String hostelBallotEndDate) {
        this.hostelBallotEndDate = hostelBallotEndDate;
    }    
        
    @PreDestroy
    public void destroyBean() {
        if(entityManager.isOpen())
            entityManager.close();
        entityManager = null;     
    }
}
