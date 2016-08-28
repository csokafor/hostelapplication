package com.perblo.hostel.bean;

import com.perblo.hostel.entity.EligibleStudent;
import java.io.Serializable;

import javax.persistence.EntityManager;

import com.perblo.hostel.entity.HostelApplication;
import com.perblo.hostel.entity.StudentFeePayment;
import com.perblo.hostel.helper.HostelApplicationHelper;
import com.perblo.hostel.listener.HostelEntityManagerListener;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.apache.log4j.Logger;

@ManagedBean(name="hostelPaymentBean")
@SessionScoped
public class HostelPaymentBean implements Serializable {
    private static final Logger log = Logger.getLogger(HostelPaymentBean.class);
    private EntityManager entityManager;
    private HostelApplication hostelApplication;
    
    @ManagedProperty(value="#{hostelApplicationHelper}")
    private HostelApplicationHelper hostelApplicationHelper;
    
    public HostelPaymentBean() {        
        this.entityManager = HostelEntityManagerListener.createEntityManager();              
    }
        
    public void hostelPayment() {                
        log.info("hostelPayment.hostelPayment() action called");        
    }

    public String confirmStatusPayment(HostelApplication hostelApplication) {
        boolean hasPaidSchoolFees = false;
        this.hostelApplication = hostelApplication;        
        EligibleStudent eligibleStudent = getEligibleStudentByStudentNumber(hostelApplication.getStudentNumber());
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
        EligibleStudent eligibleStudent = hostelApplicationHelper.getEligibleStudentByStudentNumber(hostelApplication.getStudentNumber());
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
    
    private EligibleStudent getEligibleStudentByStudentNumber(String studentNumber) {
        EligibleStudent eligibleStudent = null;
        try {
            Query query = entityManager.createNamedQuery("getEligibleStudentByStudentNumber");
            query.setParameter(1, studentNumber);
            
            eligibleStudent = (EligibleStudent)query.getSingleResult();
            
            
        } catch(Exception e) {
            log.error("Error in getEligibleStudentByStudentNumber: " + e.getLocalizedMessage());
        }
        
        return eligibleStudent;
    }
    
    /*
    public boolean confirmPayment(HostelApplication hostelApplication) {
        boolean hasPaidSchoolFees = false;
        this.hostelApplication = hostelApplication;
        
        EligibleStudent eligibleStudent = hostelApplicationHelper.getEligibleStudentByStudentNumber(hostelApplication.getStudentNumber());
        if(eligibleStudent == null) {
            hasPaidSchoolFees = false;
        } else {
            if (eligibleStudent.getHostelStudentType().getStudentType().equalsIgnoreCase("Scholarship")) {
                hasPaidSchoolFees = false;
                log.info(hostelApplication.getStudentNumber() + " is a scholarship student");
                facesMessages.add(Severity.INFO, "Scholarship students do not pay accommodation fee. "
                        + "Use your matric number and hostel pin to check your application status and complete your application.");               
               
              
            } else {
                hasPaidSchoolFees = true;
            }
            
        }
               
        return hasPaidSchoolFees;
    }
    */
    public String cancel() {
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

    public HostelApplicationHelper getHostelApplicationHelper() {
        return hostelApplicationHelper;
    }

    public void setHostelApplicationHelper(HostelApplicationHelper hostelApplicationHelper) {
        this.hostelApplicationHelper = hostelApplicationHelper;
    }    
    
}
