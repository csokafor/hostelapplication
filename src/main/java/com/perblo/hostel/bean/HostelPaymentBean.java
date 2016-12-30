package com.perblo.hostel.bean;

import com.perblo.hostel.entity.EligibleStudent;
import java.io.Serializable;
import java.util.Calendar;

import javax.persistence.EntityManager;

import com.perblo.hostel.entity.HostelApplication;
import com.perblo.hostel.entitymanager.HostelEntityManagerImpl;
import com.perblo.hostel.service.HostelApplicationService;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import com.perblo.payment.PaymentService;
import com.perblo.payment.constants.TransactionStatus;
import com.perblo.payment.entity.PaymentTransaction;
import com.perblo.payment.entity.Pin;
import org.apache.log4j.Logger;

@ManagedBean(name="hostelPaymentBean")
@SessionScoped
public class HostelPaymentBean implements Serializable {
    private static final Logger log = Logger.getLogger(HostelPaymentBean.class);

    @ManagedProperty(value = "#{hostelEntityManager}")
    HostelEntityManagerImpl hostelEntityManager;
    
    @ManagedProperty(value="#{hostelApplicationService}")
    private HostelApplicationService hostelApplicationService;

    @ManagedProperty(value="#{paymentService}")
    private PaymentService paymentService;

    private HostelApplication hostelApplication;
    private String pinNumber;
    private boolean paymentSuccessful;
    
    public HostelPaymentBean() {        

    }
        
    public void hostelPayment() {                
        log.info("hostelPayment.hostelPayment() action called");        
    }

    public String confirmStatusPayment(HostelApplication hostelApplication) {
        boolean hasPaidSchoolFees = false;
        this.hostelApplication = hostelApplication;        
        EligibleStudent eligibleStudent = hostelApplicationService.getEligibleStudentByStudentNumber(hostelApplication.getStudentNumber());
        if(eligibleStudent == null) {
            hasPaidSchoolFees = false;
            log.info(hostelApplication.getStudentNumber() + " is a scholarship student");
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "You have not paid your school charges this session, "
                        + "make both payment within a week or you will forfeit your space!", ""));               
               
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
    
    public String confirmApplicationPayment(HostelApplication hostelApplication) {
        boolean hasPaidSchoolFees = false;
        this.hostelApplication = hostelApplication;        
        EligibleStudent eligibleStudent = hostelApplicationService.getEligibleStudentByStudentNumber(hostelApplication.getStudentNumber());
        if(eligibleStudent == null) {
            hasPaidSchoolFees = false;
            log.info(hostelApplication.getStudentNumber() + " is a scholarship student");
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "You have not paid your school charges this session, "
                        + "make both payment within a week or you will forfeit your space!", ""));               
               
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
            return "checkapplicationstatus";
        }
        
    }

    public void processPinPayment() {
        log.info("pinNumber = " + pinNumber + " , application No: " + hostelApplication.getApplicationNumber());
        paymentSuccessful = false;

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
                    if(pin.getPinValue() == hostelApplication.getTotalAmount()) {
                        Calendar calendar = Calendar.getInstance();

                        paymentSuccessful =  paymentService.processPinPayment(hostelApplication, pinNumber, "Hostel Accommodation", hostelApplication.getTotalAmount());
                        if(paymentSuccessful) {
                            FacesContext.getCurrentInstance().addMessage(null,
                                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Payment was successful. Thank you for applying for your hostel accommodation.", ""));
                        } else {
                            FacesContext.getCurrentInstance().addMessage(null,
                                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Payment not successful!",""));
                        }

                    } else {
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Pin Value ("+pin.getPinValue()+") does not match the corresponding item.",""));

                    }
                }
            }
        } catch(Exception e) {
            log.error("processPinPayment Error: " + e.getMessage(), e);
        }
    }

    public String cancel() {
        log.info("HostelPaymentAction.cancel() called");
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "index";
    }

    public String close() {
        log.info("HostelPaymentAction.cancel() called");
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
        return "index";
    }

    public void setHostelApplication(HostelApplication hostelApplication) {
        this.hostelApplication = hostelApplication;
    }

    public HostelApplication getHostelApplication() {
        return hostelApplication;
    }

    public HostelApplicationService getHostelApplicationService() {
        return hostelApplicationService;
    }

    public void setHostelApplicationService(HostelApplicationService hostelApplicationService) {
        this.hostelApplicationService = hostelApplicationService;
    }

    public HostelEntityManagerImpl getHostelEntityManager() {
        return hostelEntityManager;
    }

    public void setHostelEntityManager(HostelEntityManagerImpl hostelEntityManager) {
        this.hostelEntityManager = hostelEntityManager;
    }

    public PaymentService getPaymentService() {
        return paymentService;
    }

    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    public String getPinNumber() {
        return pinNumber;
    }

    public void setPinNumber(String pinNumber) {
        this.pinNumber = pinNumber;
    }

    public boolean isPaymentSuccessful() {
        return paymentSuccessful;
    }

    public void setPaymentSuccessful(boolean paymentSuccessful) {
        this.paymentSuccessful = paymentSuccessful;
    }
}
