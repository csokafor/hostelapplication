package com.perblo.hostel.bean;

import java.io.Serializable;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.perblo.hostel.entity.HostelApplication;
import com.perblo.hostel.entity.HostelBallotApplication;
import com.perblo.hostel.entitymanager.HostelEntityManagerImpl;
import com.perblo.hostel.service.HostelApplicationStatus;
import com.perblo.hostel.service.HostelBallotService;
import com.perblo.hostel.service.HostelConfig;
import com.perblo.hostel.service.HostelSettingsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import com.perblo.payment.PaymentService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import javax.annotation.PreDestroy;
import javax.faces.bean.SessionScoped;


@ManagedBean(name="ballotStatusBean")
@SessionScoped
public class HostelBallotApplicationStatusBean implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(HostelBallotApplicationStatusBean.class);

    @ManagedProperty(value="#{hostelBallotService}")
    private HostelBallotService hostelBallotService;

    @ManagedProperty(value="#{hostelSettingsService}")
    private HostelSettingsService hostelSettingsService;

    private HostelBallotApplication ballotApplication;    
    private boolean loggedIn;         
    private String applicationNumber;    
    private String studentNumber;
    private int ballotStatus;
    private Random random = new Random();
    
    private String applicationDate = null;

    private String passportFileExtension;
    private String passportFileName; 

    private boolean hasBallotEnded;   
    private String hostelBallotEndDate;
    
    public HostelBallotApplicationStatusBean() {
    }
          
    public void checkBallotApplication() {
        try {
            ballotApplication = hostelBallotService.getHostelBallotApplicationByStudentNumber(studentNumber);

            if (ballotApplication == null) {
            	FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Your Student Number is invalid!",""));
            } else {
                
                ballotApplication = hostelBallotService.getHostelBallotApplicationByStudentNumberAndApplicationNumber(studentNumber, applicationNumber);
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
                    Date ballotEndDate = hostelSettingsService.getHostelBallotEndDate();
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
    	    hostelBallotService.getHostelEntityManager().merge(ballotApplication);
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

    public void cancel() {
        log.info("HostelBallotApplicationStatusAction.cancel() called");
    }

    public String close() {
        log.info("HostelBallotApplicationStatusAction.close() called");
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();        
        return "index";
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

    public HostelSettingsService getHostelSettingsService() {
        return hostelSettingsService;
    }

    public void setHostelSettingsService(HostelSettingsService hostelSettingsService) {
        this.hostelSettingsService = hostelSettingsService;
    }

    public HostelBallotService getHostelBallotService() {
        return hostelBallotService;
    }

    public void setHostelBallotService(HostelBallotService hostelBallotService) {
        this.hostelBallotService = hostelBallotService;
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
                DateFormat.MEDIUM, DateFormat.SHORT).format(hostelSettingsService.getHostelBallotEndDate());
        }
        return hostelBallotEndDate;
    }

    public void setHostelBallotEndDate(String hostelBallotEndDate) {
        this.hostelBallotEndDate = hostelBallotEndDate;
    }    

}
