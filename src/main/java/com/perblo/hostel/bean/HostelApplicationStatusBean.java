package com.perblo.hostel.bean;

import com.perblo.hostel.entity.EligibleStudent;
import com.perblo.hostel.entity.HostelAllocation;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Query;

import com.perblo.hostel.entity.HostelApplication;
import com.perblo.hostel.entitymanager.HostelEntityManager;
import com.perblo.hostel.entitymanager.HostelEntityManagerImpl;
import com.perblo.hostel.service.*;
import com.perblo.payment.PaymentService;
import com.perblo.payment.entity.PaymentTransaction;
import com.perblo.payment.entity.Pin;
import java.io.ByteArrayInputStream;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.Part;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@ManagedBean(name="applicationStatusBean")
@SessionScoped
public class HostelApplicationStatusBean implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(HostelApplicationStatusBean.class);

    @ManagedProperty(value = "#{hostelEntityManager}")
    HostelEntityManager hostelEntityManager;

    @ManagedProperty(value="#{hostelApplicationService}")
    private HostelApplicationService hostelApplicationService;
         
    @ManagedProperty(value="#{hostelSettingsService}")
    private HostelSettingsService hostelSettingsService;
                
    @ManagedProperty(value="#{hostelRoomAllocationService}")
    private HostelRoomAllocationService hostelRoomAllocationService;

    @ManagedProperty(value="#{paymentService}")
    private PaymentService paymentService;

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
    private String base64ApplicationNumber = "";
    private String barCodeApplicationNumber = "15000";

    private String passportFileExtension;
    private String passportFileName; 
    //private UploadedFile file;
    private Part uploadedFile;
    private DefaultStreamedContent passport;
    
    public HostelApplicationStatusBean() {
        hostelApplication = new HostelApplication();
    }   
    
    public String checkHostelApplication() {
        try {
            //check hostel application getHostelApplicationByStudentNumber
            hostelApplication = hostelApplicationService.getHostelApplicationByStudentNumber(studentNumber);

            if (hostelApplication == null) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Your Student Number is invalid!",""));                
            } else {
                if (hostelApplication.getStudentType().getStudentType().equalsIgnoreCase("Scholarship")) {
                    //check new pin
                    Pin pin = paymentService.getPinByPinNumber(applicationNumber);
                    if (pin == null) {
                        log.warn(studentNumber + " entered invalid pin");
                        FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Your entered an invalid pin!",""));                        
                    } else {
                        if (pin.isUsedStatus()) {
                            log.info(studentNumber + " pin is used");
                            PaymentTransaction paymentTx = paymentService.getPaymentTransactionByTransactionId(applicationNumber);
                            HostelApplication studentHostelApp = hostelApplicationService.getHostelApplicationByPaymentTransactionId(paymentTx.getId());
                            if(studentHostelApp == null) {
                                loggedIn = false;
                                FacesContext.getCurrentInstance().addMessage(null,
                                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Pin has been used!",""));                                
                            } else if (studentHostelApp.getStudentNumber().equalsIgnoreCase(studentNumber)) {
                                loggedIn = true;
                            }

                        } else {
                            log.info(studentNumber + " pin is not used");
                            boolean paymentSuccessful = paymentService.processPinPayment(hostelApplication, applicationNumber, "Accommodation Fee", hostelApplication.getTotalAmount());
                            if (paymentSuccessful) {
                                loggedIn = true;
                            }
                        }
                    }
                } else {
                    log.info("checkHostelApplication " + studentNumber);
                    hostelApplication = hostelApplicationService.getHostelApplicationByStudentNumberAndApplicationNumber(studentNumber, applicationNumber);

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
            Date applicationEndDate = hostelSettingsService.getHostelApplicationEndDate();
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
                
        EligibleStudent eligibleStudent = hostelApplicationService.getEligibleStudentByStudentNumber(
                hostelApplication.getStudentNumber());
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

    public void allocateHostelForHandicapStudent() {
        log.info("allocateHostelForHandicapStudent called");
        try {
            if (hostelApplication.getStudentType().getStudentType().equalsIgnoreCase("Handicap")) {
                
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "You are eligible for hostel accommodation!",""));
                hostelAllocation = hostelRoomAllocationService.getHosteRoomAllocation(hostelApplication);
                if (hostelAllocation == null) {
                    log.info("hostelAllocation is null");
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Sorry a room could not be allocated to you",""));                    
                    hostelApplication.setBallotStatus(HostelApplicationStatus.UNSUCCESSFUL);

                } else {
                    hostelApplication.setBallotStatus(HostelApplicationStatus.SUCCESSFUL);
                    hostelAllocation.setAcademicSession(hostelSettingsService.getCurrentAcademicSession());
                    hostelEntityManager.getEntityManager().persist(hostelAllocation);
                    hostelApplication.setHostelAllocation(hostelAllocation);
                }
                hostelEntityManager.getEntityManager().merge(hostelApplication);

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

    public Part getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(Part uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public void upload() {
        try {
            String fileName = getFilename(uploadedFile);
            log.info("filename: " + fileName + " , contentType: " + uploadedFile.getContentType() + " , size: " + uploadedFile.getSize());

            if (fileName.contains("jpg") || fileName.contains("jpeg") || fileName.contains("png")) {
                hostelApplication.setPassportData(IOUtils.toByteArray(uploadedFile.getInputStream()));
                hostelApplication.setPassportContentType(uploadedFile.getContentType());
                hostelApplicationService.setHostelApplicationPassport(hostelApplication, uploadedFile);

                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Passport has been uploaded.", ""));

            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please upload a jpg/jpeg/png file.", ""));
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Passport upload error.", ""));
            log.error("upload error: " + e.getMessage(), e);
        }

    }

    private String getFilename(Part part) {
        for (String cd : part.getHeader("content-disposition").split(";")) {
            if (cd.trim().startsWith("filename")) {
                String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1);

            }
        }
        return null;
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

    public String printApplicationStatus() {
        log.info("printApplicationStatus");
        setBase64ApplicationNumber(Base64.encodeBase64String(hostelApplication.getApplicationNumber().getBytes()));
        barCodeApplicationNumber = hostelApplication.getApplicationNumber().substring(1);

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

    public String cancel() {
        log.info("HostelApplicationStatusAction.cancel() called");
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();        
        return "index";
    }

    public String close() {
        log.info("HostelApplicationStatusAction.close() called");
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();        
        return "index";
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
            DateFormat.MEDIUM, DateFormat.SHORT).format(hostelSettingsService.getHostelBallotEndDate());
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

    public HostelApplicationService getHostelApplicationService() {
        return hostelApplicationService;
    }

    public void setHostelApplicationService(HostelApplicationService hostelApplicationService) {
        this.hostelApplicationService = hostelApplicationService;
    }

    public HostelSettingsService getHostelSettingsService() {
        return hostelSettingsService;
    }

    public void setHostelSettingsService(HostelSettingsService hostelSettingsService) {
        this.hostelSettingsService = hostelSettingsService;
    }

    public HostelRoomAllocationService getHostelRoomAllocationService() {
        return hostelRoomAllocationService;
    }

    public void setHostelRoomAllocationService(HostelRoomAllocationService hostelRoomAllocationService) {
        this.hostelRoomAllocationService = hostelRoomAllocationService;
    }

    public PaymentService getPaymentService() {
        return paymentService;
    }

    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public HostelEntityManager getHostelEntityManager() {
        return hostelEntityManager;
    }

    public void setHostelEntityManager(HostelEntityManager hostelEntityManager) {
        this.hostelEntityManager = hostelEntityManager;
    }

    public String getBase64ApplicationNumber() {
        return base64ApplicationNumber;
    }

    public void setBase64ApplicationNumber(String base64ApplicationNumber) {
        this.base64ApplicationNumber = base64ApplicationNumber;
    }
}
