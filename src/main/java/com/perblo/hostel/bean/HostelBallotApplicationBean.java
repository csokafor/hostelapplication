package com.perblo.hostel.bean;

import com.perblo.hostel.entity.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.application.FacesMessage.Severity;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.perblo.hostel.helper.HostelApplicationHelper;
import com.perblo.hostel.helper.HostelApplicationStatus;
import com.perblo.hostel.helper.HostelRoomAllocationHelper;
import com.perblo.hostel.helper.HostelSettingsHelper;
import com.perblo.hostel.listener.HostelEntityManagerListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import javax.annotation.PreDestroy;
import javax.faces.bean.SessionScoped;
import javax.persistence.EntityTransaction;

import org.apache.log4j.Logger;

@ManagedBean(name="ballotBean")
@SessionScoped
public class HostelBallotApplicationBean implements Serializable {
    private static final Logger log = Logger.getLogger(HostelBallotApplicationBean.class);
	
    private EntityManager entityManager;
    
    private Random random = new Random();
           
    private HostelBallotApplication ballotApplication;
          
    private EligibleBallotStudent eligibleBallotStudent;
    
    @ManagedProperty(value="#{hostelApplicationHelper}")
    private HostelApplicationHelper hostelApplicationHelper;
         
    @ManagedProperty(value="#{hostelSettingsHelper}")
    private HostelSettingsHelper hostelSettingsHelper;
                
    @ManagedProperty(value="#{hostelRoomAllocationHelper}")
    private HostelRoomAllocationHelper hostelRoomAllocationHelper;
            
    private boolean hostelApplicatonSaved;
    private boolean eligibleForAccommodation;   
    private boolean hasBallotEnded;
    private String ballotStatus;
    private String applicationStatus;
    private String hostelBallotEndDate;
    private Date ballotEndDate;
    private long facultyId;
    private long departmentId;
    private long cosId;
           
    private List<Department> departments = new ArrayList<Department>();
    private List<ProgrammeOfStudy> ProgrammeOfStudyList = new ArrayList<ProgrammeOfStudy>();
    
    public HostelBallotApplicationBean() {
    	this.entityManager = HostelEntityManagerListener.createEntityManager();
        //log.info("entityManager: " + this.entityManager.toString());
        
        ballotApplication = new HostelBallotApplication();
        //check hostel ballot end date
        /*
        Date now = new Date();
        Date ballotEndDate = hostelSettingsHelper.getHostelBallotEndDate();
        if(now.after(ballotEndDate)) {
            hasBallotEnded = true;
        } else {
            hasBallotEnded = false;
        }
        */
    }          
    
    public void confirmHostelBallotApplication() {        
        log.info("hostelBallotApplication.getStudentNumber():" + ballotApplication.getStudentNumber());
        eligibleForAccommodation = true;
        
        //check for hostel application eligibility
        if (ballotApplication.getId() == null) {
            HostelBallotApplication oldApplication = hostelApplicationHelper.getHostelBallotApplicationByAcademicSessionAndStudentNumber(
                    hostelSettingsHelper.getCurrentAcademicSession().getId(), ballotApplication.getStudentNumber());

            if (oldApplication != null) {
                eligibleForAccommodation = false;
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "You have already submitted ballot application for this session!",""));
                
                log.info("Old application found in database!");  
                return;
            }
        }
        eligibleBallotStudent = hostelApplicationHelper.getEligibleBallotStudentByStudentNumber(ballotApplication.getStudentNumber());        

        Calendar calendar = Calendar.getInstance();
        ballotApplication.setAcademicSession(hostelSettingsHelper.getCurrentAcademicSession());
        ballotApplication.setApplicationDate(calendar.getTime());
        ballotApplication.setApplicationStatus(HostelApplicationStatus.NOT_SUBMITTED);
        ballotApplication.setBallotStatus(HostelApplicationStatus.UNSUCCESSFUL);

        if(facultyId > 0) {
            ballotApplication.setFaculty(entityManager.find(Faculty.class, facultyId));
            if(departmentId > 0) {
                ballotApplication.setDepartment(entityManager.find(Department.class, departmentId));
                if(cosId > 0) {
                    ballotApplication.setProgrammeOfStudy(entityManager.find(ProgrammeOfStudy.class, cosId));
                }
            }
        }

        if (eligibleBallotStudent == null) {       
            eligibleForAccommodation = false;
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_WARN, "You are not eligible for hostel ballot!",""));            
            log.info("Not eigible for hostel accomodation!");
        } else {
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "You are eligible for hostel ballot, click on ballot button",""));            
            log.info("Eligible for hostel ballot!");
        }
        
    }
    
    public void saveHostelBallot() {
        try {           
            
            ballotApplication.setApplicationStatus(HostelApplicationStatus.SUBMITTED);            
            hostelApplicatonSaved = true;
            
            //do hostel ballot
            Department department = eligibleBallotStudent.getDepartment();
            
            HostelBallotQuota hostelBallotQuota = hostelApplicationHelper.getHostelBallotQuotaMap().get(department.getId());
            if(hostelBallotQuota != null) {
                boolean ballotSucces = hostelApplicationHelper.doHostelBallot(eligibleBallotStudent);
                if(ballotSucces) {
                    ballotApplication.setBallotStatus(HostelApplicationStatus.SUCCESSFUL);
                } else {
                    ballotApplication.setBallotStatus(HostelApplicationStatus.UNSUCCESSFUL);
                }
                
            } else {
            	FacesContext.getCurrentInstance().addMessage(null,
    					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ballot for accommodation failed!", ""));                
                log.error("HostelBallotQuota not found for department - " + department.getName());
            }       
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            ballotApplication.setApplicationNumber(generateApplicationNumber(hostelSettingsHelper.getBallotNumber()));
            entityManager.persist(ballotApplication);                                                           
            transaction.commit();
            
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Your ballot form has been saved, you can check your ballot status "
                        + "with your Student Number("+ ballotApplication.getStudentNumber() +") "
                        + "and Ballot Number("+ ballotApplication.getApplicationNumber() + ")", ""));
            
            log.info("New Ballot Application: for " + ballotApplication.getStudentNumber() + ", ballot Number: " + ballotApplication.getApplicationNumber());

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Your ballot form could not be saved!", ""));            
            log.error(e.getMessage(), e);            
        }

    }
    
    public String close() {
        log.info("HostelBallotApplicationAction.close() called");
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();        
        return "checkballotstatus";
    }
   
    public String generateApplicationNumber(Integer id) {
        Integer requestNumber = 150000;
               
        requestNumber = requestNumber + id;

        log.info("requestNumber = " + requestNumber);
        String applicationId = "B" + requestNumber.toString();
        
        return applicationId;
    }         
         
    public String getApplicationStatus() {
        return HostelApplicationStatus.getApplicationStatus(ballotApplication.getApplicationStatus());
    }
    
    public void setBallotStatus(String status) {
        this.ballotStatus = HostelApplicationStatus.getBallotStatus(ballotApplication.getBallotStatus());
    }
    
    public String getBallotStatus() {
        return HostelApplicationStatus.getBallotStatus(ballotApplication.getBallotStatus());
    }

    public void setHostelApplicatonSaved(boolean hostelApplicatonSaved) {
        this.hostelApplicatonSaved = hostelApplicatonSaved;
    }

    public boolean isHostelApplicatonSaved() {
        return hostelApplicatonSaved;
    }
              
    public boolean isEligibleForAccommodation() {
        return eligibleForAccommodation;
    }

    public void setEligibleForAccommodation(boolean eligibleForAccommodation) {
        this.eligibleForAccommodation = eligibleForAccommodation;
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
        
    public HostelBallotApplication getBallotApplication() {
        return ballotApplication;
    }

    public void setBallotApplication(HostelBallotApplication ballotApplication) {
        this.ballotApplication = ballotApplication;
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

    //@Begin(join = true)
    public void updateDepartmentList() {
        log.info("updateDepartmentList() " + facultyId);
        if (facultyId > 0) {
            Faculty faculty = ballotApplication.getFaculty();
            departments = hostelApplicationHelper.getDepartmentByFacultyId(facultyId);
            log.info("departments list = " + departments.size());
        }

    }

    //@Begin(join = true)
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

    public boolean getHasBallotEnded() {
        //check hostel ballot end date
        if(ballotEndDate == null) {
            Date now = new Date();
            ballotEndDate = hostelSettingsHelper.getHostelBallotEndDate();
            if(now.after(ballotEndDate)) {
                hasBallotEnded = true;                
            } else {
                hasBallotEnded = false;        
            }            
        }
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
    
    @PreDestroy
    public void destroyBean() {
        if(entityManager.isOpen())
            entityManager.close();
        entityManager = null;
        departments = null;
        ProgrammeOfStudyList = null;
    }

}
