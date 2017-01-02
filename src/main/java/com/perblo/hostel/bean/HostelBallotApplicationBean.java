package com.perblo.hostel.bean;

import com.perblo.hostel.entity.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;

import com.perblo.hostel.entitymanager.HostelEntityManagerImpl;
import com.perblo.hostel.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import javax.annotation.PreDestroy;
import javax.faces.bean.SessionScoped;
import javax.persistence.EntityTransaction;


@ManagedBean(name="ballotBean")
@SessionScoped
public class HostelBallotApplicationBean implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(HostelBallotApplicationBean.class);

    private Random random = new Random();
           
    private HostelBallotApplication ballotApplication;
          
    private EligibleBallotStudent eligibleBallotStudent;

    @ManagedProperty(value="#{hostelBallotService}")
    private HostelBallotService hostelBallotService;

    @ManagedProperty(value="#{hostelApplicationService}")
    private HostelApplicationService hostelApplicationService;
         
    @ManagedProperty(value="#{hostelSettingsService}")
    private HostelSettingsService hostelSettingsService;

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

    private List<Faculty> faculties = new ArrayList<Faculty>();
    private List<Department> departments = new ArrayList<Department>();
    private List<ProgrammeOfStudy> ProgrammeOfStudyList = new ArrayList<ProgrammeOfStudy>();
    
    public HostelBallotApplicationBean() {
        ballotApplication = new HostelBallotApplication();
    }

    @PostConstruct
    public void init() {
        faculties = hostelApplicationService.getFaculties();
    }
    
    public void confirmHostelBallotApplication() {        
        log.info("hostelBallotApplication.getStudentNumber():" + ballotApplication.getStudentNumber());
        try {
            eligibleForAccommodation = true;

            //check for hostel application eligibility
            if (ballotApplication.getId() == null) {
                HostelBallotApplication oldApplication = hostelBallotService.getHostelBallotApplicationByAcademicSessionAndStudentNumber(
                        hostelSettingsService.getCurrentAcademicSession().getId(), ballotApplication.getStudentNumber());

                if (oldApplication != null) {
                    eligibleForAccommodation = false;
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_WARN, "You have already submitted ballot application for this session!", ""));

                    log.info("Old application found in database!");
                    return;
                }
            }
            eligibleBallotStudent = hostelBallotService.getEligibleBallotStudentByStudentNumber(ballotApplication.getStudentNumber());

            Calendar calendar = Calendar.getInstance();
            ballotApplication.setAcademicSession(hostelSettingsService.getCurrentAcademicSession());
            ballotApplication.setApplicationDate(calendar.getTime());
            ballotApplication.setApplicationStatus(HostelApplicationStatus.NOT_SUBMITTED);
            ballotApplication.setBallotStatus(HostelApplicationStatus.UNSUCCESSFUL);

            if (facultyId > 0) {
                ballotApplication.setFaculty(hostelBallotService.getHostelEntityManager().findById(Faculty.class, facultyId));
                if (departmentId > 0) {
                    ballotApplication.setDepartment(hostelBallotService.getHostelEntityManager().findById(Department.class, departmentId));
                    log.info("departmentId " + departmentId + " name " + ballotApplication.getDepartment().getName());
                    if (cosId > 0) {
                        ballotApplication.setProgrammeOfStudy(hostelBallotService.getHostelEntityManager().findById(ProgrammeOfStudy.class, cosId));
                    }
                }
            }

            if (eligibleBallotStudent == null) {
                eligibleForAccommodation = false;
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "You are not eligible for hostel ballot!", ""));
                log.info("Not eigible for hostel ballot!");
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "You are eligible for hostel ballot, click on ballot button", ""));
                log.info("Eligible for hostel ballot!");
            }

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "", "Server Error. Please try again later"));
            log.error("confirmHostelBallotApplication Error: " + e.getMessage(), e);
        }
        
    }
    
    public void saveHostelBallot() {
        try {           
            
            ballotApplication.setApplicationStatus(HostelApplicationStatus.SUBMITTED);            
            hostelApplicatonSaved = true;
            
            //do hostel ballot
            Department department = eligibleBallotStudent.getDepartment();
            
            HostelBallotQuota hostelBallotQuota = hostelBallotService.getHostelBallotQuotaMap().get(department.getId());
            if(hostelBallotQuota != null) {
                boolean ballotSucces = hostelBallotService.doHostelBallot(eligibleBallotStudent);
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

            ballotApplication.setApplicationNumber(generateApplicationNumber(hostelSettingsService.getBallotNumber()));
            hostelBallotService.getHostelEntityManager().persist(ballotApplication);

            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Your ballot form has been saved, you can check your ballot status "
                        + "with your Student Number("+ ballotApplication.getStudentNumber() +") "
                        + "and Ballot Number("+ ballotApplication.getApplicationNumber() + ")", ""));
            
            log.info("New Ballot Application: for " + ballotApplication.getStudentNumber() + ", ballot Number: " + ballotApplication.getApplicationNumber());

        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Your ballot form could not be saved!", ""));
            log.error(e.getMessage(), e);            
        }

    }
    
    public String close() {
        log.info("HostelBallotApplicationAction.close() called");
        FacesContext.getCurrentInstance().getExternalContext().invalidateSession();        
        return "checkballotstatus";
    }
   
    public String generateApplicationNumber(Integer id) {
        Integer requestNumber = 170000;
               
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

    public HostelApplicationService getHostelApplicationService() {
        return hostelApplicationService;
    }

    public void setHostelApplicationService(HostelApplicationService hostelApplicationService) {
        this.hostelApplicationService = hostelApplicationService;
    }

    public HostelBallotService getHostelBallotService() {
        return hostelBallotService;
    }

    public void setHostelBallotService(HostelBallotService hostelBallotService) {
        this.hostelBallotService = hostelBallotService;
    }

    public HostelSettingsService getHostelSettingsService() {
        return hostelSettingsService;
    }

    public void setHostelSettingsService(HostelSettingsService hostelSettingsService) {
        this.hostelSettingsService = hostelSettingsService;
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
        //if (departments.size() == 0) {
        //    departments = hostelApplicationService.getDepartments();
        //}
        return departments;
    }

    public void updateDepartmentList() {
        log.info("updateDepartmentList() " + facultyId);
        if (facultyId > 0) {
            Faculty faculty = ballotApplication.getFaculty();
            departments = hostelApplicationService.getDepartmentByFacultyId(facultyId);
            log.info("departments list = " + departments.size());
        }

    }

    public void updateProgrammeOfStudyList() {
        log.info("updateProgrammeOfStudyList()" + departmentId);
        if (departmentId > 0) {
            setProgrammeOfStudyList(hostelApplicationService.getProgrammeOfStudyByDepartmentId(departmentId));
            log.info("ProgrammeOfStudyList list = " + ProgrammeOfStudyList.size());
        }
    }

    public void setProgrammeOfStudyList(List<ProgrammeOfStudy> programmeOfStudyList) {
        ProgrammeOfStudyList = programmeOfStudyList;
    }

    public List<ProgrammeOfStudy> getProgrammeOfStudyList() {
        //if (ProgrammeOfStudyList.size() == 0) {
        //    ProgrammeOfStudyList = hostelApplicationService.getProgrammeOfStudy();
        //}
        return ProgrammeOfStudyList;
    }

    public List<Faculty> getFaculties() {
        return faculties;
    }

    public void setFaculties(List<Faculty> faculties) {
        this.faculties = faculties;
    }

    public boolean getHasBallotEnded() {
        //check hostel ballot end date
        if(ballotEndDate == null) {
            Date now = new Date();
            log.info("hostelSettingsService " + hostelSettingsService);
            ballotEndDate = hostelSettingsService.getHostelBallotEndDate();
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
                DateFormat.MEDIUM, DateFormat.SHORT).format(hostelSettingsService.getHostelBallotEndDate());
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
        departments = null;
        ProgrammeOfStudyList = null;
    }

}
