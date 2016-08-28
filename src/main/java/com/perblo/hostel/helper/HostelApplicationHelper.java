package com.perblo.hostel.helper;

import com.perblo.hostel.bean.HostelDAO;
import com.perblo.hostel.entity.*;
import com.perblo.hostel.listener.HostelEntityManagerListener;
import java.io.Serializable;

import java.util.*;
import javax.annotation.PreDestroy;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import org.apache.log4j.Logger;


@ManagedBean(name="hostelApplicationHelper")
@ApplicationScoped
public class HostelApplicationHelper implements Serializable {
	private static final Logger log = Logger.getLogger(HostelApplicationHelper.class);
    public static String USER_ADMIN_ROLE = "User Admin";
    public static String TRANSCRIPT_ADMIN_ROLE = "Transcript Admin";
    public static String REQUEST_ADMIN_ROLE = "Request Admin";
    public static String RECORDS_ADMIN_ROLE = "Records Admin";
    public static String DATA_ENTRY = "Data Entry";
    public static String TRANSCRIPT_APPROVAL_ADMIN_ROLE = "Transcript Approval Admin";
    
    public static String SCHOLARSHIP = "Scholarship";
    public static String POST_GRADUATE = "Post Graduate";
    public static String MEDICAL_STUDENT = "Medical Student";
          
    private EntityManager entityManager;
    
    //@ManagedProperty(value="#{message}")
    //private Message messageBean;
    
    private List<Role> roles;
    private List<Faculty> faculties = null;
    private List<Department> departments = null;
    private List<Nationality> nationalities = null;
    private List<ProgrammeOfStudy> programmeOfStudy = null;
    private List<Semester> semesters;
    private List<AcademicSession> academicSession;
    private List<Hostel> hostels = null;
    private List<HostelRoomBedSpace> hostelRoomBedSpaces = null;
    
    private List<SchoolYear> yearList;
    
    private List<String> genderList = new ArrayList<String>();
    private List<String> modeOfEntryList = new ArrayList<String>();
    private List<String> degreeClassList = new ArrayList<String>();
    private List<Course> courses;
    private List<CourseTitle> courseTitles;
    
    private List<Hostel> hostelList;
    private static Map<Long,HostelBallotQuota> hostelBallotQuotaMap = null;
    private final Object hostelBallotLock = new Object();
    private Random random;

    public HostelApplicationHelper() {
    	
    	this.entityManager = HostelEntityManagerListener.createEntityManager();
        log.info("HostelApplicationHelper entityManager: " + this.entityManager.toString());
        
        /*
        int currentYear = new java.util.GregorianCalendar().get(GregorianCalendar.YEAR);    	
        for(int i = 1970;i <= currentYear;i++) {
        yearList.add(new Integer(i).toString());
        }
         */

        genderList.add("Male");
        genderList.add("Female");

        modeOfEntryList.add("JAMB");
        modeOfEntryList.add("Direct Entry");

        degreeClassList.add("First Class");
        degreeClassList.add("Second Class Upper");
        degreeClassList.add("Second Class Lower");
        degreeClassList.add("Third Class");
        degreeClassList.add("Pass");
        
        random = new Random();
    }

    public Student getStudentByStudentNumber(String studentNumber) {
        Student student = null;
        Query query = entityManager.createNamedQuery("getStudentByStudentNumber");
        query.setParameter(1, studentNumber);
        List results = query.getResultList();
        if (results.size() > 0) {
            student = (Student) results.get(0);
        }
        return student;
    }
    
    public EligibleBallotStudent getEligibleBallotStudentByStudentNumber(String studentNumber) {
        EligibleBallotStudent eligibleBallotStudent = null;
        Query query = entityManager.createNamedQuery("getEligibleBallotStudentByStudentNumber");
        query.setParameter(1, studentNumber);
        List results = query.getResultList();
        if (results.size() > 0) {
            eligibleBallotStudent = (EligibleBallotStudent) results.get(0);
        }
        return eligibleBallotStudent;
    }
    
    public EligibleStudent getEligibleStudentByStudentNumber(String studentNumber) {
        EligibleStudent eligibleStudent = null;
        try {
            Query query = entityManager.createNamedQuery("getEligibleStudentByStudentNumber");
            query.setParameter(1, studentNumber);
            
            eligibleStudent = (EligibleStudent)query.getSingleResult();
            
            
        } catch(NoResultException nre) {
            log.warn("getEligibleStudentByStudentNumber no result found: " + studentNumber);
        } catch(Exception e) {
            log.error("getEligibleStudentByStudentNumber: " + e.getMessage(), e);
        }
        
        return eligibleStudent;
    }
    
    public StudentFeePayment getStudentFeePaymentByStudentNumber(String studentNumber) {
        StudentFeePayment studentFeePayment = null;
        Query query = entityManager.createNamedQuery("getStudentFeePaymentByStudentNumber");
        query.setParameter(1, studentNumber);
        List results = query.getResultList();
        if (results.size() > 0) {
            studentFeePayment = (StudentFeePayment) results.get(0);
        }
        return studentFeePayment;
    }

    public HostelApplication getHostelApplicationByStudentNumber(String studentNumber) {
        HostelApplication hostelApplication = null;
        Query query = entityManager.createNamedQuery("getHostelApplicationByStudentNumber");
        query.setParameter(1, studentNumber);
        List results = query.getResultList();
        if (results.size() > 0) {
            hostelApplication = (HostelApplication) results.get(0);
        }
        return hostelApplication;
    }
       
    public HostelApplication getHostelApplicationByAcademicSessionAndStudentNumber(Integer sessionId, String studentNumber) {
        HostelApplication hostelApplication = null;
        try {
            Query query = entityManager.createNamedQuery("getHostelApplicationByAcademicSessionAndStudentNumber");
            query.setParameter(1, sessionId);
            query.setParameter(2, studentNumber);
            List results = query.getResultList();
            if (results.size() > 0) {
                hostelApplication = (HostelApplication) results.get(0);
            }
                        
        } catch(NoResultException nre) {
            log.warn("getHostelApplicationByAcademicSessionAndStudentNumber: no result found: " + studentNumber);
        } catch(Exception e) {
            log.error(e.getMessage(), e);
        }
        return hostelApplication;
    }
    
    public HostelBallotApplication getHostelBallotApplicationByAcademicSessionAndStudentNumber(Integer sessionId, String studentNumber) {
        HostelBallotApplication hostelBallotApplication = null;
        Query query = entityManager.createNamedQuery("getHostelBallotApplicationByAcademicSessionAndStudentNumber");
        query.setParameter(1, sessionId);
        query.setParameter(2, studentNumber);
        List results = query.getResultList();
        if (results.size() > 0) {
            hostelBallotApplication = (HostelBallotApplication) results.get(0);
        }
        return hostelBallotApplication;
    }
    
    public HostelAllocation getHostelAllocationByStudentNumberandAcademicSession(Integer sessionId, String studentNumber) {
        HostelAllocation hostelAllocation = null;
        Query query = entityManager.createNamedQuery("getHostelAllocationByStudentNumberandAcademicSession");
        query.setParameter(1, studentNumber);
        query.setParameter(2, sessionId);        
        List results = query.getResultList();
        if (results.size() > 0) {
            hostelAllocation = (HostelAllocation) results.get(0);
        }
        return hostelAllocation;
    }
    
    public HostelStudentType getHostelStudentTypeByStudentType(String studentType) {
        HostelStudentType hostelStudentType = null;
        Query query = entityManager.createNamedQuery("getHostelStudentTypeByStudentType");
        query.setParameter(1, studentType);        
        List results = query.getResultList();
        if (results.size() > 0) {
            hostelStudentType = (HostelStudentType) results.get(0);
        }
        return hostelStudentType;
    }
    
    public List<Hostel> getHostelList() {
        List<Hostel> hostelList = null;
        Query query = entityManager.createNamedQuery("getAllHostels");        
        hostelList = (List<Hostel>)query.getResultList();
       
        return hostelList;
    }
        

    public HostelApplication getHostelApplicationByApplicationNumber(String applicationNumber) {
        HostelApplication hostelApplication = null;
        Query query = entityManager.createNamedQuery("getHostelApplicationByApplicationNumber");
        query.setParameter(1, applicationNumber);
        List results = query.getResultList();
        if (results.size() > 0) {
            hostelApplication = (HostelApplication) results.get(0);
        }
        return hostelApplication;
    }
    
    public HostelBallotApplication getHostelBallotApplicationByApplicationNumber(String applicationNumber) {
        HostelBallotApplication hostelBallotApplication = null;
        Query query = entityManager.createNamedQuery("getHostelBallotApplicationByApplicationNumber");
        query.setParameter(1, applicationNumber);
        List results = query.getResultList();
        if (results.size() > 0) {
            hostelBallotApplication = (HostelBallotApplication) results.get(0);
        }
        return hostelBallotApplication;
    }

    public void setFaculties(List<Faculty> faculties) {
        this.faculties = faculties;
    }

    public List<Faculty> getFaculties() {
        if (faculties == null) {
            Query query = entityManager.createNamedQuery("getAllFaculties");
            faculties = query.getResultList();
        }
        return faculties;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public List<Department> getDepartments() {
        if (departments == null) {
            Query query = entityManager.createNamedQuery("getAllDepartments");
            departments = query.getResultList();
        }

        return departments;
    }

    public void setProgrammeOfStudy(List<ProgrammeOfStudy> programmeOfStudy) {
        this.programmeOfStudy = programmeOfStudy;
    }

    public List<ProgrammeOfStudy> getProgrammeOfStudy() {
        if (programmeOfStudy == null) {
            Query query = entityManager.createNamedQuery("getAllProgrammeOfStudy");
            programmeOfStudy = query.getResultList();
        }
        return programmeOfStudy;
    }

    public List<Department> getDepartmentByFacultyId(Long facultyId) {
        Query query = entityManager.createNamedQuery("getDepartmentByFacultyId");
        query.setParameter(1, facultyId);
        departments = query.getResultList();
        return departments;
    }

    public List<ProgrammeOfStudy> getProgrammeOfStudyByDepartmentId(Long departmentId) {
        Query query = entityManager.createNamedQuery("getProgrammeOfStudyByDepartmentId");
        query.setParameter(1, departmentId);
        programmeOfStudy = query.getResultList();
        return programmeOfStudy;
    }

    public Faculty getFacultyByName(String facultyName) {
        Query query = entityManager.createNamedQuery("getFacultyByName");
        query.setParameter(1, facultyName);
        return (Faculty) query.getResultList().get(0);
    }

    public void setYearList(List<SchoolYear> yearList) {
        this.yearList = yearList;
    }

    public List<SchoolYear> getYearList() {
        if (yearList == null) {
            Query query = entityManager.createNamedQuery("getAllSchoolYear");
            yearList = (List<SchoolYear>) query.getResultList();
            //log.info("yearList size = " + yearList.size());
        }
        return yearList;
    }

    public List<HostelRoomBedSpace> getHostelRoomBedSpaces() {
        if(hostelRoomBedSpaces == null) {
            Query query = entityManager.createNamedQuery("getAllHostelRoomBedSpace");
            hostelRoomBedSpaces = (List<HostelRoomBedSpace>) query.getResultList();
        }
        return hostelRoomBedSpaces;
    }

    public void setHostelRoomBedSpaces(List<HostelRoomBedSpace> hostelRoomBedSpaces) {
        this.hostelRoomBedSpaces = hostelRoomBedSpaces;
    }
    
    

    public Map<Long, HostelBallotQuota> getHostelBallotQuotaMap() {
        if(hostelBallotQuotaMap == null) {
            hostelBallotQuotaMap = new HashMap<Long, HostelBallotQuota>();
            Query query = entityManager.createNamedQuery("getAllHostelBallotQuota");
            List<HostelBallotQuota> hostelQuotas = (List<HostelBallotQuota>) query.getResultList();
            
            for(HostelBallotQuota hostelQuota : hostelQuotas) {
                hostelBallotQuotaMap.put(hostelQuota.getDepartment().getId(), hostelQuota);
            }
        }
        return hostelBallotQuotaMap;
    }

    private void setHostelBallotQuotaMap(Map<Long, HostelBallotQuota> hostelBallotQuotaMap) {
        HostelApplicationHelper.hostelBallotQuotaMap = hostelBallotQuotaMap;
    }
    
    private void updateHostelBallotQuotaMap(HostelBallotQuota hostelBallotQuota) {
        hostelBallotQuotaMap.put(hostelBallotQuota.getDepartment().getId(), hostelBallotQuota);
    }
    
    public boolean doHostelBallot(EligibleBallotStudent eligibleBallotStudent) {
        boolean ballotStatus = false;
        try {
            Department department = eligibleBallotStudent.getDepartment();
            HostelBallotQuota hostelBallotQuota = getHostelBallotQuotaMap().get(department.getId());
                                    
            synchronized(hostelBallotLock) {
                EntityTransaction transaction = entityManager.getTransaction();
                transaction.begin();
                if (eligibleBallotStudent.getGender().equalsIgnoreCase("Male") || eligibleBallotStudent.getGender().equalsIgnoreCase("M")) {
                    if (hostelBallotQuota.getUsedMaleQuota() < hostelBallotQuota.getNumberOfMaleStudents()) {
                        int randNo = random.nextInt(99);
                        log.info("hostelballot random no " + randNo);
                        if (randNo > 45) {
                            ballotStatus = true;
                            log.info(eligibleBallotStudent.getStudentNumber() + " ballot succesful" + randNo);
                            hostelBallotQuota.setUsedMaleQuota(hostelBallotQuota.getUsedMaleQuota() + 1);
                            entityManager.merge(hostelBallotQuota);
                            updateHostelBallotQuotaMap(hostelBallotQuota);
                        } else {
                            log.info(eligibleBallotStudent.getStudentNumber() + " ballot not succesful." + randNo);
                        }
                    } else {
                        log.info(department.getName() + " male quota filled");                    
                    }
                } else if(eligibleBallotStudent.getGender().equalsIgnoreCase("Female") || eligibleBallotStudent.getGender().equalsIgnoreCase("F")){
                    if (hostelBallotQuota.getUsedFemaleQuota() < hostelBallotQuota.getNumberOfFemaleStudents()) {
                        int randNo = random.nextInt(6);
                        if (randNo > 2) {
                            ballotStatus = true;
                            log.info(eligibleBallotStudent.getStudentNumber() + " ballot succesful"+ randNo);
                            hostelBallotQuota.setUsedFemaleQuota(hostelBallotQuota.getUsedFemaleQuota() + 1);
                            entityManager.merge(hostelBallotQuota);
                            updateHostelBallotQuotaMap(hostelBallotQuota);
                        } else {
                             log.info(eligibleBallotStudent.getStudentNumber() + " ballot not succesful." + randNo);
                        }
                    } else {
                        log.info(department.getName() + " female quota filled");
                        ballotStatus = false;
                    }
                } else {
                	FacesContext.getCurrentInstance().addMessage(null,
        					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ballot for accommodation failed!",""));
                    log.error("Unknown gender for " + eligibleBallotStudent.getStudentNumber());
                }
                transaction.commit();
            }
            
        } catch(Exception e) {
        	FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ballot for accommodation failed!", ""));            
            log.error(e.getMessage());
        }
        
        return ballotStatus;
    }

    public void setSemesters(List<Semester> semesters) {
        this.semesters = semesters;
    }

    public List<Semester> getSemesters() {
        if (semesters == null) {
            Query query = entityManager.createNamedQuery("getAllSemesters");
            semesters = (List<Semester>) query.getResultList();
        }
        return semesters;
    }

    public void setAcademicSession(List<AcademicSession> academicSession) {
        this.academicSession = academicSession;
    }

    public List<AcademicSession> getAcademicSession() {
        if (academicSession == null) {
            Query query = entityManager.createNamedQuery("getAllAcademicSession");
            academicSession = (List<AcademicSession>) query.getResultList();
        }
        return academicSession;
    }

    public List<Hostel> getHostels() {
        if(hostels == null) {
            Query query = entityManager.createNamedQuery("getAllHostels");
            hostels = (List<Hostel>) query.getResultList();
        }
        return hostels;
    }

    public void setHostels(List<Hostel> hostels) {
        this.hostels = hostels;
    }
    
    

    public void setCourses(List<Course> courses) {
        this.courses = courses;
    }

    public List<Course> getCourses() {
        if (courses == null) {
            Query query = entityManager.createNamedQuery("getAllCourses");
            courses = (List<Course>) query.getResultList();
        }
        return courses;
    }

    public List<CourseTitle> getCourseTitlesByCourseId(long courseId) {
        Query query = entityManager.createNamedQuery("getCourseTitleByCourseId");
        query.setParameter(1, courseId);
        courseTitles = query.getResultList();
        return courseTitles;
    }

    public CourseTitle getCourseTitlesByCourseTitleAndCode(String title, String courseCode) {
        CourseTitle courseTitle = null;
        Query query = entityManager.createNamedQuery("getCourseTitleByCourseTitleAndCode");
        query.setParameter(1, title);
        query.setParameter(2, courseCode);
        List resultList = query.getResultList();
        if (resultList.size() > 0) {
            courseTitle = (CourseTitle) resultList.get(0);
        }
        return courseTitle;
    }

    public void setCourseTitles(List<CourseTitle> courseTitles) {
        this.courseTitles = courseTitles;
    }

    public List<CourseTitle> getCourseTitles() {
        return courseTitles;
    }

    public void setGenderList(List<String> genderList) {
        this.genderList = genderList;
    }

    public List<String> getGenderList() {
        return genderList;
    }

    public void setModeOfEntryList(List<String> modeOfEntryList) {
        this.modeOfEntryList = modeOfEntryList;
    }

    public List<String> getModeOfEntryList() {
        return modeOfEntryList;
    }

    public void setDegreeClassList(List<String> degreeClassList) {
        this.degreeClassList = degreeClassList;
    }

    public List<String> getDegreeClassList() {
        return degreeClassList;
    }

    public void setNationalities(List<Nationality> nationalities) {
        this.nationalities = nationalities;
    }

    public List<Nationality> getNationalities() {
        if (nationalities == null) {
            Query query = entityManager.createNamedQuery("getAllNationalities");
            nationalities = query.getResultList();
        }

        return nationalities;
    }
   
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public List<Role> getRoles() {
        if (roles == null) {
            Query query = entityManager.createNamedQuery("getAllRoles");
            roles = query.getResultList();
        }
        return roles;
    }

    public List<User> getUserByRole(String roleName) {
        List<User> userList = new ArrayList<User>();

        Query query = entityManager.createNamedQuery("getUsersByRole");
        query.setParameter(1, roleName);
        userList = query.getResultList();

        return userList;
    }

    public User getUserByUsername(String username) {
        User user = null;

        Query query = entityManager.createNamedQuery("getUserByUsername");
        query.setParameter(1, username);
        List resultList = query.getResultList();
        if (resultList.size() > 0) {
            user = (User) resultList.get(0);
        }

        return user;
    }

    @PreDestroy
    public void destroyBean() {
        entityManager.close();        
    }
}
