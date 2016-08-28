package com.perblo.hostel.bean;

import com.perblo.hostel.entity.AcademicSession;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import com.perblo.hostel.entity.Department;
import com.perblo.hostel.entity.EligibleStudent;
import com.perblo.hostel.entity.Faculty;
import com.perblo.hostel.entity.HostelAllocation;
import com.perblo.hostel.entity.ProgrammeOfStudy;
import com.perblo.hostel.entity.HostelApplication;
import com.perblo.hostel.entity.HostelRoom;
import com.perblo.hostel.entity.HostelStudentType;
import com.perblo.hostel.helper.HostelApplicationHelper;
import com.perblo.hostel.helper.HostelApplicationStatus;
import com.perblo.hostel.helper.HostelRoomAllocationHelper;
import com.perblo.hostel.helper.HostelSettingsHelper;
import com.perblo.hostel.listener.HostelEntityManagerListener;
import com.perblo.payment.PaymentHelper;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
import javax.persistence.Query;
import org.apache.log4j.Logger;

@ManagedBean(name="applicationBean")
@SessionScoped
public class HostelApplicationBean implements Serializable {

    private static final Logger log = Logger.getLogger(HostelApplicationBean.class);
    private EntityManager entityManager;
    
    private Random random = new Random();
    private HostelApplication hostelApplication;        
    private HostelAllocation hostelAllocation;    
    private boolean studentTypeSelected;
    private String hostelStudentType;    
    private EligibleStudent eligibleStudent;
    Date applicationEndDate;
    
    @ManagedProperty(value="#{hostelApplicationHelper}")
    private HostelApplicationHelper hostelApplicationHelper;
         
    @ManagedProperty(value="#{hostelSettingsHelper}")
    private HostelSettingsHelper hostelSettingsHelper;
                
    @ManagedProperty(value="#{hostelRoomAllocationHelper}")
    private HostelRoomAllocationHelper hostelRoomAllocationHelper;
                
    private boolean hostelApplicatonSaved;
    private boolean eligibleForAccommodation;
    private String paymentStatus;
    private String ballotStatus;
    private String applicationStatus;
    private long facultyId;
    private long departmentId;
    private long cosId;
           
    private List<Department> departments = new ArrayList<Department>();
    private List<ProgrammeOfStudy> ProgrammeOfStudyList = new ArrayList<ProgrammeOfStudy>();
    
    private boolean hasApplicationEnded;   
    private String hostelApplicationEndDate;
    
    public HostelApplicationBean() {
    	this.entityManager = HostelEntityManagerListener.createEntityManager();
        //log.info("entityManager: " + this.entityManager.toString());     
        
        hostelApplication = new HostelApplication();
    }

    //@Begin(join = true)
    public void startHostelApplication() {
        log.info("startHostelApplication() called");
        hostelApplication = new HostelApplication();
    }
        
    public void selectStudentType() {
        log.info("selectStudentType() called ");
        this.setStudentTypeSelected(true);

    }
    
    public String confirmHostelApplication() {
        String studentNumber = hostelApplication.getStudentNumber().trim();
        log.info("HostelApplicationAction.confirmHostelApplication(): hostelStudentType = " + hostelStudentType);
        log.info("hostelApplication.getStudentNumber():" + studentNumber);

        eligibleForAccommodation = true;
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            //check for hostel application eligibility
            if (hostelApplication.getId() == null) {
                HostelApplication oldApplication = hostelApplicationHelper.getHostelApplicationByAcademicSessionAndStudentNumber(
                        hostelSettingsHelper.getCurrentAcademicSession().getId(), studentNumber);

                if (oldApplication != null) {
                    eligibleForAccommodation = false;
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "You have already submitted hostel application for this session!",""));                
                    log.info("Old application found in database!");
                    return "hostelapplication";
                }
            }

            eligibleStudent = getEligibleStudentByStudentNumber(studentNumber);        

            Calendar calendar = Calendar.getInstance();
            hostelApplication.setAcademicSession(hostelSettingsHelper.getCurrentAcademicSession());
            hostelApplication.setApplicationDate(calendar.getTime());
            hostelApplication.setApplicationStatus(HostelApplicationStatus.NOT_SUBMITTED);
            hostelApplication.setStudentType(getHostelStudentType(hostelStudentType));        
            hostelApplication.setPaymentStatus(HostelApplicationStatus.NOT_PAID);
            hostelApplication.setPaymentTransactionId(-1);

            if(facultyId > 0) {
                hostelApplication.setFaculty(entityManager.find(Faculty.class, facultyId));
                if(departmentId > 0) {
                    hostelApplication.setDepartment(entityManager.find(Department.class, departmentId));
                    if(cosId > 0) {
                        hostelApplication.setProgrammeOfStudy(entityManager.find(ProgrammeOfStudy.class, cosId));
                    } else {
                        FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Select your course of study",""));
                        return "hostelapplication";
                    }
                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Select your department",""));
                    return "hostelapplication";
                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "Select your faculty",""));
                return "hostelapplication";
            }

            if (eligibleStudent != null) {
                 if(!eligibleStudent.getHostelStudentType().getStudentType().equalsIgnoreCase(hostelStudentType)) {         
                    eligibleForAccommodation = false;
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "You choose an incorrect student category. Your category is " 
                            + eligibleStudent.getHostelStudentType().getStudentType(),""));                    
                    log.warn("Incorrect student type!");
                    return "hostelapplication";
                }

                if (!hostelApplication.getStudentType().getStudentType().equalsIgnoreCase("Handicap")) {
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "You are eligible for hostel accommodation!",""));                    

                    hostelAllocation = getHostelAllocationByStudentNumberandAcademicSession(hostelApplication.getStudentNumber(), hostelSettingsHelper.getCurrentAcademicSession());
                    if (hostelAllocation == null) {
                        log.info("hostelAllocation is null");
                        FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Sorry a room has not be allocated to you.",""));                        
                        hostelApplication.setBallotStatus(HostelApplicationStatus.UNSUCCESSFUL);

                    } else {
                        hostelApplication.setTotalAmount(hostelApplication.getStudentType().getHostelFee());
                        hostelApplication.setBallotStatus(HostelApplicationStatus.SUCCESSFUL);                    
                        hostelApplication.setHostelAllocation(hostelAllocation);
                    }

                } else {
                    hostelAllocation = getHostelAllocationByStudentNumberandAcademicSession(hostelApplication.getStudentNumber(), hostelSettingsHelper.getCurrentAcademicSession());
                    hostelApplication.setTotalAmount(hostelApplication.getStudentType().getHostelFee());
                    hostelApplication.setBallotStatus(HostelApplicationStatus.SUCCESSFUL);
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "You are a handicapped student kindly go to students affairs office for allocation of space","")); 

                }
                transaction.begin();
                if (hostelApplication.getId() == null) {                    
                    hostelApplication.setApplicationNumber(generateApplicationNumber(hostelSettingsHelper.getApplicationNumber()));
                    entityManager.persist(hostelApplication);                    
                } else {                   
                    entityManager.merge(hostelApplication);                    
                }
                transaction.commit();
                
            } else {            
                eligibleForAccommodation = false;
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "You are not eligible for Hostel Accommodation!",""));                
                log.info("Not eigible for hostel accomodation!");
                return "hostelapplication";
            }
        } catch(Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Your hostel application could not be saved!",""));
            log.error(e.getMessage(), e);
            transaction.rollback();
            return "hostelapplication";
        } 
          
        return "applicationconfirmation";

    }

    public void saveHostelApplication() {
        EntityTransaction transaction = entityManager.getTransaction();
        try {           
            hostelApplication.setApplicationDate(new Date());
            hostelApplication.setApplicationStatus(HostelApplicationStatus.SUBMITTED);           
            hostelApplication.setTotalAmount(hostelApplication.getStudentType().getHostelFee());
            hostelApplication.setPaymentStatus(HostelApplicationStatus.NOT_PAID);
            hostelApplication.setPaymentTransactionId(-1);
                                    
            transaction.begin();
            hostelApplication.setApplicationNumber(generateApplicationNumber(hostelSettingsHelper.getApplicationNumber()));
            entityManager.persist(hostelApplication);        
            transaction.commit();
            
            hostelApplicatonSaved = true;
            //transaction.begin();
            //hostelApplication.setApplicationNumber(generateApplicationNumber(hostelApplication.getId()));            
            //entityManager.merge(hostelApplication);
            //transaction.commit();
            
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Your application has been saved, you can check your application status with your Student Number("
                            + hostelApplication.getStudentNumber() + ") and Application Number(" + hostelApplication.getApplicationNumber() + ")",""));             
            log.info("New Hostel Application: " + hostelApplication.getId() + " for " + hostelApplication.getFirstName() + " " + hostelApplication.getLastName());

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Your hostel application could not be saved!",""));             
            log.error(e.getMessage(), e);     
            transaction.rollback();
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
            return "checkapplicationstatus";
        }
        
    }

    public String editHostelApplication() {
        log.info("HostelApplicationAction.editHostelApplication() called");
        return "hostelapplication";
    }
            
    public void doAccommodationBallot() {
        log.info("HostelApplicationAction.doAccommodationBallot() called");
        try {
            int ballotNo = random.nextInt(5);
            if(ballotNo <= 2) {
                log.info("ballotNo = " + ballotNo + ". Ballot succesful");
                
                hostelAllocation = hostelRoomAllocationHelper.getHosteRoomAllocation(hostelApplication);
                if(hostelAllocation == null) {
                    log.info("hostelAllocation is null");
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Sorry a room could not be allocated to you",""));                    
                    hostelApplication.setBallotStatus(HostelApplicationStatus.UNSUCCESSFUL);
                    
                } else {
                    hostelApplication.setBallotStatus(HostelApplicationStatus.SUCCESSFUL);
                    FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Congratulations you won the ballot for accommodation. Please pay for your accommodation",""));
                    
                    hostelAllocation = hostelRoomAllocationHelper.getHosteRoomAllocation(hostelApplication);               
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
            
            hostelApplication.setApplicationDate(new Date());
            hostelApplication.setApplicationStatus(HostelApplicationStatus.SUBMITTED);

            //set payment details
            hostelApplication.setTotalAmount(hostelSettingsHelper.getAccommodationFee());
            hostelApplication.setPaymentStatus(HostelApplicationStatus.NOT_PAID);
            hostelApplication.setPaymentTransactionId(-1);
            
            if(hostelApplication.getId() == null) {
                hostelApplication.setApplicationNumber(generateApplicationNumber(hostelSettingsHelper.getApplicationNumber()));
                entityManager.persist(hostelApplication);
                //hostelApplication.setApplicationNumber(generateApplicationNumber(hostelApplication.getId()));
                //entityManager.merge(hostelApplication);
            } else {
                entityManager.merge(hostelApplication);
            }
            
            
        } catch(Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ballot for accommodation failed!",""));             
            log.error(e.getMessage());
        }
    }
    
    public void backToApplicationConfirmation() {
        log.info("HostelApplicationAction.backToApplicationConfirmation() called");
    }
    
    //@End
    public String cancel() {
        log.info("HostelApplicationAction.cancel() called");
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();        
        return "index";
    }

    //@End
    public String close() {
        log.info("HostelApplicationAction.close() called");
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();        
        return "index";
    }
   
    public String generateApplicationNumber(int id) {
        Long requestNumber = 15000000L;               
        requestNumber = requestNumber + id;

        log.info("requestNumber = " + requestNumber);
        return "H" + requestNumber.toString();
    }
    
        
    private HostelStudentType getHostelStudentType(String studentType) {
        HostelStudentType hStudentType = null;
        try {
            Query query = entityManager.createNamedQuery("getHostelStudentTypeByStudentType");
            query.setParameter(1, studentType);
            
            hStudentType = (HostelStudentType)query.getSingleResult();
            
            
        } catch(Exception e) {
            log.error("Error in getHostelStudentType: " + e.getLocalizedMessage());
        }
        
        return hStudentType;
    }
    
    private HostelAllocation getHostelAllocationByStudentNumberandAcademicSession(String studentNumber, AcademicSession academicSession) {
        HostelAllocation allocation = null;
        try {
            Query query = entityManager.createNamedQuery("getHostelAllocationByStudentNumberandAcademicSession");
            query.setParameter(1, studentNumber);
            query.setParameter(2, academicSession.getId());
            
            allocation = (HostelAllocation)query.getSingleResult();
            
            
        } catch(Exception e) {
            log.error("Error in getHostelAllocationByStudentNumberandAcademicSession: " + e.getLocalizedMessage());
        }
        
        return allocation;
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
    
    private boolean checkEligibility() {
        boolean isEligible = false;
        try {
            String matricNumber = hostelApplication.getStudentNumber();
            String otherStudentEligibleMatricNo = hostelSettingsHelper.getOtherStudentEligibleMatricNo();
            String medicalStudentEligibleMatricNo = hostelSettingsHelper.getMedicalStudentEligibleMatricNo();
            
            //check if student is medical student
            if(hostelApplication.getDepartment().getName().equalsIgnoreCase("Medicine and Surgery")) {
                log.info(matricNumber + " is a medical student. eligible for accommodation");
                String subMatricNumber = matricNumber.substring(0, 3);
                if(medicalStudentEligibleMatricNo.contains(subMatricNumber)) {
                    isEligible = true;
                    log.info(matricNumber + " is a medical student. eligible for accommodation");
                }
                
            } else {
                String subMatricNumber = matricNumber.substring(0, 3);
                if(otherStudentEligibleMatricNo.contains(subMatricNumber)) {
                    isEligible = true;
                    log.info(matricNumber + " is a final year student. eligible for accommodation");
                } else {
                    //check for first year student
                    if(!matricNumber.contains("/")) {
                        log.info(matricNumber + " is a first year student. eligible for accommodation");
                        isEligible = true;
                    }
                }
            }
            
        } catch(Exception e) {
            log.error("Exception in checkEligibility: " + e.getMessage());
        }
        
        return isEligible;
    }
    
    private boolean checkValidStudentNumber() {
        boolean studentNumberValid = false;
        try {
            String yStudy = hostelApplication.getYearOfStudy();
            String matricNumber = hostelApplication.getStudentNumber();
            
            if(yStudy.startsWith("6")) {
                if(matricNumber.startsWith("06/")) {
                    studentNumberValid = true;
                }
            } else if(yStudy.startsWith("5")) {
                if(matricNumber.startsWith("07/")) {
                    studentNumberValid = true;
                }
            } else if(yStudy.startsWith("4")) {
                if(matricNumber.startsWith("08/")) {
                    studentNumberValid = true;
                }
            } else if(yStudy.startsWith("3")) {
                if(matricNumber.startsWith("09/")) {
                    studentNumberValid = true;
                }
            } else if(yStudy.startsWith("2")) {
                if(matricNumber.startsWith("10/")) {
                    studentNumberValid = true;
                }
            } else if(yStudy.startsWith("1")) {
                if(!matricNumber.contains("/")) {
                    studentNumberValid = true;
                }
                
            }
            
            
        }catch(Exception e) {
            log.error("Exception in checkValidStudentNumber: " + e.getMessage());
        }
        
        return studentNumberValid;
    }

    //@Asynchronous
    /*
    public void sendHostelApplicationMail() {
        try {
            String schoolName = hostelSettingsHelper.getSchoolName();
            MailMessage mailMessage = new MailMessage();
            mailMessage.setAttachment(false);
            mailMessage.setMimeType("text/html");
            mailMessage.setSubject(schoolName + " Transcript Request Information");
            mailMessage.setRecipients(hostelApplication.getEmail());
            mailMessage.setSender(hostelSettingsHelper.getHostelEmail());

            String message = "<br> Dear " + hostelApplication.getFirstName() + " " + hostelApplication.getLastName() + ",<br><br>" +
                    "Your transcript request has been saved, you can check your request status with your Student Number = " + hostelApplication.getStudentNumber() +
                    " and Request Number = " + hostelApplication.getApplicationNumber() +
                    ".<br>Note that you have to pay the transcript fee =N=" + hostelApplication.getTotalAmount() + " before your transcript request is processed.<br><br> " +
                    "Regards,<br>Transcript Department<br>" + schoolName;

            mailMessage.setMessageText(message);
            MailSender mailSender = new MailSender("java:/TranscriptMail");
            boolean mailStatus = mailSender.sendMail(mailMessage);
            if (!mailStatus) {
                log.error("Email sending failed!");
                //facesMessages.add(Severity.ERROR, "Email sending failed.");
            }

        } catch (Exception e) {
            log.error("Email sending failed!");
            //facesMessages.add(Severity.ERROR, "Email sending failed: " + e.getMessage());
        }
    }
    */
    
    public String getApplicationStatus() {
        return HostelApplicationStatus.getApplicationStatus(hostelApplication.getApplicationStatus());
    }

    public String getPaymentStatus() {
        return HostelApplicationStatus.getPaymentStatus(hostelApplication.getPaymentStatus());
    }
    
    public String getBallotStatus() {
        return HostelApplicationStatus.getBallotStatus(hostelApplication.getBallotStatus());
    }

    public void setHostelApplicatonSaved(boolean hostelApplicatonSaved) {
        this.hostelApplicatonSaved = hostelApplicatonSaved;
    }

    public boolean isHostelApplicatonSaved() {
        return hostelApplicatonSaved;
    }

    public EligibleStudent getEligibleStudent() {
        return eligibleStudent;
    }

    public void setEligibleStudent(EligibleStudent eligibleStudent) {
        this.eligibleStudent = eligibleStudent;
    }
    
          
    public boolean isEligibleForAccommodation() {
        return eligibleForAccommodation;
    }

    public void setEligibleForAccommodation(boolean eligibleForAccommodation) {
        this.eligibleForAccommodation = eligibleForAccommodation;
    }

    public boolean isStudentTypeSelected() {
        return studentTypeSelected;
    }

    public void setStudentTypeSelected(boolean studentTypeSelected) {
        this.studentTypeSelected = studentTypeSelected;
    }

    public String getHostelStudentType() {
        return hostelStudentType;
    }

    public void setHostelStudentType(String hostelStudentType) {
        this.hostelStudentType = hostelStudentType;
    }
        
    
 
    public void setTranscriptRequestHelper(HostelApplicationHelper hostelApplicationHelper) {
        this.hostelApplicationHelper = hostelApplicationHelper;
    }

    public HostelApplicationHelper getTranscriptRequestHelper() {
        return hostelApplicationHelper;
    }

    public void setTranscriptSettingsHelper(HostelSettingsHelper hostelSettingsHelper) {
        this.hostelSettingsHelper = hostelSettingsHelper;
    }

    public HostelSettingsHelper getTranscriptSettingsHelper() {
        return hostelSettingsHelper;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public List<Department> getDepartments() {
        if (departments.size() == 0) {
            departments = hostelApplicationHelper.getDepartments();
        }
        return departments;
    }    
    
    public void updateDepartmentList() {
        log.info("updateDepartmentList() " + facultyId);
        if (facultyId > 0) {            
            departments = hostelApplicationHelper.getDepartmentByFacultyId(facultyId);
            log.info("departments list = " + departments.size());
        }
    }
    
    public void updateProgrammeOfStudyList() {
        log.info("updateProgrammeOfStudyList()" + departmentId);
        if (departmentId > 0) {
            setProgrammeOfStudyList(hostelApplicationHelper.getProgrammeOfStudyByDepartmentId(departmentId));
            log.info("ProgrammeOfStudyList list = " + ProgrammeOfStudyList.size());
        }
    }

    public void setProgrammeOfStudyList(List<ProgrammeOfStudy> programmeOfStudyList) {
        ProgrammeOfStudyList = programmeOfStudyList;
    }

    public List<ProgrammeOfStudy> getProgrammeOfStudyList() {
        if (ProgrammeOfStudyList.size() == 0) {
            ProgrammeOfStudyList = hostelApplicationHelper.getProgrammeOfStudy();
        }
        return ProgrammeOfStudyList;
    }

    public boolean isHasApplicationEnded() {
        //check hostel ballot end date
        if(applicationEndDate == null) {
            Date now = new Date();
            applicationEndDate = hostelSettingsHelper.getHostelApplicationEndDate();
            if(now.after(applicationEndDate)) {
                hasApplicationEnded = true;
            } else {
                hasApplicationEnded = false;
            }
        }
        return hasApplicationEnded;
    }

    public void setHasApplicationEnded(boolean hasApplicationEnded) {
        this.hasApplicationEnded = hasApplicationEnded;
    }

    public String getHostelApplicationEndDate() {
         if(hostelApplicationEndDate == null) {            
            hostelApplicationEndDate = SimpleDateFormat.getDateTimeInstance(
                DateFormat.MEDIUM, DateFormat.SHORT).format(hostelSettingsHelper.getHostelApplicationEndDate());
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

    public HostelApplication getHostelApplication() {
        return hostelApplication;
    }

    public void setHostelApplication(HostelApplication hostelApplication) {
        this.hostelApplication = hostelApplication;
    }

    public HostelAllocation getHostelAllocation() {
        return hostelAllocation;
    }

    public void setHostelAllocation(HostelAllocation hostelAllocation) {
        this.hostelAllocation = hostelAllocation;
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
        
    @PreDestroy
    public void destroyBean() {
        if(entityManager.isOpen())
            entityManager.close();
        entityManager = null;
        departments = null;
        ProgrammeOfStudyList = null;
    }
}
