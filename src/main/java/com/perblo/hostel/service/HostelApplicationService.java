package com.perblo.hostel.service;

import com.perblo.hostel.entity.*;
import com.perblo.hostel.entitymanager.HostelEntityManager;
import com.perblo.hostel.entitymanager.HostelEntityManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;


@Service(value="hostelApplicationService")
@Transactional
public class HostelApplicationService implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(HostelApplicationService.class);

    public static String USER_ADMIN_ROLE = "User Admin";
    public static String TRANSCRIPT_ADMIN_ROLE = "Transcript Admin";
    public static String REQUEST_ADMIN_ROLE = "Request Admin";
    public static String RECORDS_ADMIN_ROLE = "Records Admin";
    public static String DATA_ENTRY = "Data Entry";
    public static String TRANSCRIPT_APPROVAL_ADMIN_ROLE = "Transcript Approval Admin";
    
    public static String SCHOLARSHIP = "Scholarship";
    public static String POST_GRADUATE = "Post Graduate";
    public static String MEDICAL_STUDENT = "Medical Student";

    @Autowired
    HostelEntityManager hostelEntityManager;

    @Autowired
    private HostelSettingsService hostelSettingsService;

    private List<Role> roles;
    private List<Faculty> faculties = null;
    private List<Department> departments = null;
    private List<ProgrammeOfStudy> programmeOfStudy = null;
    private List<Semester> semesters;
    private List<AcademicSession> academicSession;
    private List<Hostel> hostels = null;
    private List<HostelRoomBedSpace> hostelRoomBedSpaces = null;
    
    private List<SchoolYear> yearList;
    
    private List<String> genderList = new ArrayList<String>();
    private List<String> modeOfEntryList = new ArrayList<String>();
    private List<String> degreeClassList = new ArrayList<String>();

    private List<Hostel> hostelList;
    private Random random;

    public HostelApplicationService() {

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

    public EligibleStudent addEligibleStudent(String studentNumber, String studentName, String department,
                                               HostelStudentType hostelStudentType, String hostel, String uploadedBy) {
        EligibleStudent eligibleStudent = new EligibleStudent();
        try {
            eligibleStudent.setStudentNumber(studentNumber);
            eligibleStudent.setFirstName(studentName);
            eligibleStudent.setDepartment(department);
            eligibleStudent.setHostelStudentType(hostelStudentType);

            eligibleStudent.setAcademicSession(hostelSettingsService.getCurrentAcademicSession().getSessionName());
            eligibleStudent.setDateUploaded(Calendar.getInstance().getTime());
            eligibleStudent.setUploadedBy(uploadedBy);

            hostelEntityManager.persist(eligibleStudent);
            log.info("Eligible student created for " + studentNumber);

            String[] hostelArray = hostel.split(" ");
            String hostelName = hostelArray[0] + " " +  hostelArray[1];
            String roomNumber = hostelArray[3];
            String space = hostelArray[4] + " " +  hostelArray[5];
            log.info("hostel = " + hostel + " , room number = " + roomNumber + " , space = " + space);
            Hostel hall = getHostelByName(hostelName);
            HostelRoomBedSpace bedSpace = this.getHostelRoomBedSpaceByPosition(space);
            HostelRoom hostelRoom = this.getHostelRoomByHostelAndRoomNumber(hall, roomNumber);
            if(hostelRoom == null) {
                hostelRoom = new HostelRoom();
                hostelRoom.setHostel(hall);
                hostelRoom.setRoomNumber(roomNumber);
                hostelRoom.setNumberOfOccupants(4);
                hostelEntityManager.persist(hostelRoom);
            }

            HostelAllocation hostelAllocation = new HostelAllocation();
            hostelAllocation.setStudentNumber(studentNumber);
            hostelAllocation.setHostelRoomBedSpace(bedSpace);
            hostelAllocation.setHostelRoom(hostelRoom);
            hostelAllocation.setAcademicSession(hostelSettingsService.getCurrentAcademicSession());
            hostelEntityManager.persist(hostelAllocation);
            log.info("Hostel allocation created for " + studentNumber);

        } catch(Exception e) {
            log.error("Error addEligibleStudent: " + e.getMessage());
        }

        return eligibleStudent;
    }

    public HostelRoomBedSpace getHostelRoomBedSpaceByPosition(String space) {
        HostelRoomBedSpace hostelRoomBedSpace = null;
        try {
            Query query = hostelEntityManager.getEntityManager().createNamedQuery("getHostelRoomBedSpaceByPosition");
            query.setParameter(1, space);

            hostelRoomBedSpace = (HostelRoomBedSpace)query.getSingleResult();


        } catch(Exception e) {
            log.error("Error in getHostelRoomBedSpaceByPosition: " + e.getLocalizedMessage());
        }

        return hostelRoomBedSpace;
    }

    public Hostel getHostelByName(String hostelName) {
        Hostel hostel = null;
        try {
            Query query = hostelEntityManager.getEntityManager().createNamedQuery("getHostelByName");
            query.setParameter(1, hostelName);

            hostel = (Hostel)query.getSingleResult();


        } catch(Exception e) {
            log.error("Error in getHostelByName: " + e.getLocalizedMessage());
        }

        return hostel;
    }

    public HostelRoom getHostelRoomByHostelAndRoomNumber(Hostel hostel, String roomNumber) {
        HostelRoom hostelRoom = null;
        try {
            Query query = hostelEntityManager.getEntityManager().createNamedQuery("getHostelRoomByHostelAndRoomNumber");
            query.setParameter(1, hostel.getId());
            query.setParameter(2, roomNumber);

            hostelRoom = (HostelRoom)query.getSingleResult();


        } catch(Exception e) {
            log.error("Error in getHostelRoomByHostelAndRoomNumber: " + e.getLocalizedMessage());
        }

        return hostelRoom;
    }


    public HostelStudentType getHostelStudentType(String studentType) {
        HostelStudentType hStudentType = null;
        try {
            Query query = hostelEntityManager.getEntityManager().createNamedQuery("getHostelStudentTypeByStudentType");
            query.setParameter(1, studentType);

            hStudentType = (HostelStudentType)query.getSingleResult();


        } catch(Exception e) {
            log.error("Error in getHostelStudentType: " + e.getLocalizedMessage());
        }

        return hStudentType;
    }

    public HostelAllocation getHostelAllocationByStudentNumberandAcademicSession(String studentNumber, AcademicSession academicSession) {
        HostelAllocation allocation = null;
        try {
            Query query = hostelEntityManager.getEntityManager().createNamedQuery("getHostelAllocationByStudentNumberandAcademicSession");
            query.setParameter(1, studentNumber);
            query.setParameter(2, academicSession.getId());

            allocation = (HostelAllocation)query.getSingleResult();


        } catch(Exception e) {
            log.error("Error in getHostelAllocationByStudentNumberandAcademicSession: " + e.getLocalizedMessage());
        }

        return allocation;
    }

    public HostelApplication getHostelApplicationByStudentNumberAndApplicationNumber(String studentNumber, String applicationNumber) {
        HostelApplication hostelApp = null;
        try {
            Query queryObj = hostelEntityManager.getEntityManager().createNamedQuery("getHostelApplicationByStudentNumberAndApplicationNumber");
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


    public HostelApplication getHostelApplicationByPaymentTransactionId(long paymentTransactionId) {
        HostelApplication hostelApp = null;
        try {
            Query queryObj = hostelEntityManager.getEntityManager().createNamedQuery("getHostelApplicationByPaymentTransactionId");
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

    public Student getStudentByStudentNumber(String studentNumber) {
        Student student = null;
        Query query = hostelEntityManager.getEntityManager().createNamedQuery("getStudentByStudentNumber");
        query.setParameter(1, studentNumber);
        List results = query.getResultList();
        if (results.size() > 0) {
            student = (Student) results.get(0);
        }
        return student;
    }
    

    public EligibleStudent getEligibleStudentByStudentNumber(String studentNumber) {
        EligibleStudent eligibleStudent = null;
        try {
            Query query = hostelEntityManager.getEntityManager().createNamedQuery("getEligibleStudentByStudentNumber");
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
        Query query = hostelEntityManager.getEntityManager().createNamedQuery("getStudentFeePaymentByStudentNumber");
        query.setParameter(1, studentNumber);
        List results = query.getResultList();
        if (results.size() > 0) {
            studentFeePayment = (StudentFeePayment) results.get(0);
        }
        return studentFeePayment;
    }

    public HostelApplication getHostelApplicationByStudentNumber(String studentNumber) {
        HostelApplication hostelApplication = null;
        Query query = hostelEntityManager.getEntityManager().createNamedQuery("getHostelApplicationByStudentNumber");
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
            Query query = hostelEntityManager.getEntityManager().createNamedQuery("getHostelApplicationByAcademicSessionAndStudentNumber");
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
    

    public HostelAllocation getHostelAllocationByStudentNumberandAcademicSession(Integer sessionId, String studentNumber) {
        HostelAllocation hostelAllocation = null;
        Query query = hostelEntityManager.getEntityManager().createNamedQuery("getHostelAllocationByStudentNumberandAcademicSession");
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
        Query query = hostelEntityManager.getEntityManager().createNamedQuery("getHostelStudentTypeByStudentType");
        query.setParameter(1, studentType);        
        List results = query.getResultList();
        if (results.size() > 0) {
            hostelStudentType = (HostelStudentType) results.get(0);
        }
        return hostelStudentType;
    }
    
    public List<Hostel> getHostelList() {
        List<Hostel> hostelList = null;
        Query query = hostelEntityManager.getEntityManager().createNamedQuery("getAllHostels");
        hostelList = (List<Hostel>)query.getResultList();
       
        return hostelList;
    }
        

    public HostelApplication getHostelApplicationByApplicationNumber(String applicationNumber) {
        HostelApplication hostelApplication = null;
        Query query = hostelEntityManager.getEntityManager().createNamedQuery("getHostelApplicationByApplicationNumber");
        query.setParameter(1, applicationNumber);
        List results = query.getResultList();
        if (results.size() > 0) {
            hostelApplication = (HostelApplication) results.get(0);
        }
        return hostelApplication;
    }
    
    public HostelBallotApplication getHostelBallotApplicationByApplicationNumber(String applicationNumber) {
        HostelBallotApplication hostelBallotApplication = null;
        Query query = hostelEntityManager.getEntityManager().createNamedQuery("getHostelBallotApplicationByApplicationNumber");
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
            Query query = hostelEntityManager.getEntityManager().createNamedQuery("getAllFaculties");
            faculties = query.getResultList();
        }
        return faculties;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

    public List<Department> getDepartments() {
        if (departments == null) {
            Query query = hostelEntityManager.getEntityManager().createNamedQuery("getAllDepartments");
            departments = query.getResultList();
        }

        return departments;
    }

    public void setProgrammeOfStudy(List<ProgrammeOfStudy> programmeOfStudy) {
        this.programmeOfStudy = programmeOfStudy;
    }

    public List<ProgrammeOfStudy> getProgrammeOfStudy() {
        if (programmeOfStudy == null) {
            Query query = hostelEntityManager.getEntityManager().createNamedQuery("getAllProgrammeOfStudy");
            programmeOfStudy = query.getResultList();
        }
        return programmeOfStudy;
    }

    public List<Department> getDepartmentByFacultyId(Long facultyId) {
        Query query = hostelEntityManager.getEntityManager().createNamedQuery("getDepartmentByFacultyId");
        query.setParameter(1, facultyId);
        departments = query.getResultList();
        return departments;
    }

    public List<ProgrammeOfStudy> getProgrammeOfStudyByDepartmentId(Long departmentId) {
        Query query = hostelEntityManager.getEntityManager().createNamedQuery("getProgrammeOfStudyByDepartmentId");
        query.setParameter(1, departmentId);
        programmeOfStudy = query.getResultList();
        return programmeOfStudy;
    }

    public Faculty getFacultyByName(String facultyName) {
        Query query = hostelEntityManager.getEntityManager().createNamedQuery("getFacultyByName");
        query.setParameter(1, facultyName);
        return (Faculty) query.getResultList().get(0);
    }

    public void setYearList(List<SchoolYear> yearList) {
        this.yearList = yearList;
    }

    public List<SchoolYear> getYearList() {
        if (yearList == null) {
            Query query = hostelEntityManager.getEntityManager().createNamedQuery("getAllSchoolYear");
            yearList = (List<SchoolYear>) query.getResultList();
            //log.info("yearList size = " + yearList.size());
        }
        return yearList;
    }

    public List<HostelRoomBedSpace> getHostelRoomBedSpaces() {
        if(hostelRoomBedSpaces == null) {
            Query query = hostelEntityManager.getEntityManager().createNamedQuery("getAllHostelRoomBedSpace");
            hostelRoomBedSpaces = (List<HostelRoomBedSpace>) query.getResultList();
        }
        return hostelRoomBedSpaces;
    }

    public void setHostelRoomBedSpaces(List<HostelRoomBedSpace> hostelRoomBedSpaces) {
        this.hostelRoomBedSpaces = hostelRoomBedSpaces;
    }

    public HostelEntityManager getHostelEntityManager() {
        return hostelEntityManager;
    }

    public void setHostelEntityManager(HostelEntityManager hostelEntityManager) {
        this.hostelEntityManager = hostelEntityManager;
    }

    public void setSemesters(List<Semester> semesters) {
        this.semesters = semesters;
    }

    public List<Semester> getSemesters() {
        if (semesters == null) {
            Query query = hostelEntityManager.getEntityManager().createNamedQuery("getAllSemesters");
            semesters = (List<Semester>) query.getResultList();
        }
        return semesters;
    }

    public void setAcademicSession(List<AcademicSession> academicSession) {
        this.academicSession = academicSession;
    }

    public List<AcademicSession> getAcademicSession() {
        if (academicSession == null) {
            Query query = hostelEntityManager.getEntityManager().createNamedQuery("getAllAcademicSession");
            academicSession = (List<AcademicSession>) query.getResultList();
        }
        return academicSession;
    }

    public List<Hostel> getHostels() {
        if(hostels == null) {
            Query query = hostelEntityManager.getEntityManager().createNamedQuery("getAllHostels");
            hostels = (List<Hostel>) query.getResultList();
        }
        return hostels;
    }

    public void setHostels(List<Hostel> hostels) {
        this.hostels = hostels;
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

    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    public List<Role> getRoles() {
        if (roles == null) {
            Query query = hostelEntityManager.getEntityManager().createNamedQuery("getAllRoles");
            roles = query.getResultList();
        }
        return roles;
    }

    public List<User> getUserByRole(String roleName) {
        List<User> userList = new ArrayList<User>();

        Query query = hostelEntityManager.getEntityManager().createNamedQuery("getUsersByRole");
        query.setParameter(1, roleName);
        userList = query.getResultList();

        return userList;
    }

    public User getUserByUsername(String username) {
        User user = null;

        Query query = hostelEntityManager.getEntityManager().createNamedQuery("getUserByUsername");
        query.setParameter(1, username);
        List resultList = query.getResultList();
        if (resultList.size() > 0) {
            user = (User) resultList.get(0);
        }

        return user;
    }


}
