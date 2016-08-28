package com.perblo.hostel.helper;

import com.perblo.hostel.bean.HostelDAO;
import com.perblo.hostel.entity.AcademicSession;

import java.util.List;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.perblo.hostel.entity.HostelSetting;
import com.perblo.hostel.listener.HostelEntityManagerListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import javax.annotation.PreDestroy;
import javax.persistence.EntityTransaction;
import javax.persistence.PersistenceContext;

@ManagedBean(name="hostelSettingsHelper")
@ApplicationScoped
public class HostelSettingsHelper {
	private static final Logger log = Logger.getLogger(HostelSettingsHelper.class);
	
    public static String ACCOMMODATION_FEE = "ACCOMMODATION_FEE";
    public static String SCHOOL_NAME = "SCHOOL_NAME";
    public static String CURRENT_ACADEMICSESSION = "CURRENT_ACADEMICSESSION";
    public static String HOSTEL_EMAIL = "HOSTEL_EMAIL";
    public static String MEDICALSTUDENT_ELIGIBLE_MATRICNO = "MEDICALSTUDENT_ELIGIBLE_MATRICNO";
    public static String OTHERSTUDENT_ELIGIBLE_MATRICNO = "OTHERSTUDENT_ELIGIBLE_MATRICNO";
    
    public static String PRINT_SCHOOL_HEADER = "PRINT_SCHOOL_HEADER";
    public static String HOSTEL_PAGE_HEADER = "HOSTEL_PAGE_HEADER";
    public static String HOSTEL_PAGE_FOOTER = "HOSTEL_PAGE_FOOTER";
    public static String SCHOOL_LOGO = "SCHOOL_LOGO";
    public static String BALLOT_NUMBER = "BALLOT_NUMBER";
    public static String APPLICATION_NUMBER = "APPLICATION_NUMBER";
    
    public static String HOSTEL_BALLOT_ENDDATE = "HOSTEL_BALLOT_ENDDATE";
    public static String HOSTEL_APPLICATION_ENDDATE = "HOSTEL_APPLICATION_ENDDATE";
            
    private EntityManager entityManager;
              
    private String schoolName;
    private double accommodationFee;
    private AcademicSession currentAcademicSession;
    private String hostelEmail;
    private String otherStudentEligibleMatricNo;
    private String medicalStudentEligibleMatricNo;
    private static int ballotNumber;
    private static int applicationNumber;
    private final Object ballotNumberLock = new Object();
    private final Object applicationNumberLock = new Object();
    private Date hostelBallotEndDate;
    private Date hostelApplicationEndDate;

    public HostelSettingsHelper() {        
    	this.entityManager = HostelEntityManagerListener.createEntityManager();
        log.info("HostelSettingsHelper entityManager: " + this.entityManager.toString());
    }

    public String getHostelSettingByName(String name) {
        String settingValue = null;
        Query query = entityManager.createNamedQuery("getHostelSettingByName");
        query.setParameter(1, name);
        List results = query.getResultList();
        if (results.size() > 0) {
            settingValue = ((HostelSetting) results.get(0)).getSettingValue();
            //log.info("Setting - " + name + " = " + settingValue);
        } else {
            log.error("Setting - " + name + "not found.");
        }
        return settingValue;
    }
    
    public HostelSetting getHostelSetting(String name) {
        HostelSetting settingValue = null;
        Query query = entityManager.createNamedQuery("getHostelSettingByName");
        query.setParameter(1, name);
        List results = query.getResultList();
        if (results.size() > 0) {
            settingValue = (HostelSetting) results.get(0);
            //log.info("Setting - " + name + " = " + settingValue);
        } else {
            log.error("Setting - " + name + "not found.");
        }
        return settingValue;
    }

    public String getSchoolName() {
        if (schoolName == null) {
            schoolName = this.getHostelSettingByName(SCHOOL_NAME);
        }
        return schoolName;
    }

    public void setAccommodationFee(double accommodationFee) {
        this.accommodationFee = accommodationFee;
    }

    public double getAccommodationFee() {
        String amount = this.getHostelSettingByName(ACCOMMODATION_FEE);
        if (amount != null) {
            accommodationFee = Double.parseDouble(amount);
        }
        return accommodationFee;
    }
   
    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }
   
    public AcademicSession getCurrentAcademicSession() {
        if (currentAcademicSession == null) {
            String sessionId = this.getHostelSettingByName(CURRENT_ACADEMICSESSION);
            currentAcademicSession = entityManager.find(AcademicSession.class, Integer.valueOf(sessionId));
        }
        return currentAcademicSession;
    }

    public void setCurrentAcademicSession(AcademicSession currentAcademicSession) {
        this.currentAcademicSession = currentAcademicSession;
    }

    public int getBallotNumber() {
        int newBallotNumber = 0;
        
        HostelSetting hostelSetting = this.getHostelSetting(BALLOT_NUMBER);
        String ballotNo = hostelSetting.getSettingValue();
        if (ballotNo != null) {
            ballotNumber = Integer.parseInt(ballotNo);
        }  

        synchronized(ballotNumberLock) {
            newBallotNumber = ++ballotNumber;
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            hostelSetting.setSettingValue(Integer.toString(ballotNumber));
            entityManager.merge(hostelSetting);
            transaction.commit();
        }        
        
        log.info("New ballot number: " + newBallotNumber);
        return newBallotNumber;
    }

    public void setBallotNumber(int ballotNumber) {
        this.ballotNumber = ballotNumber;
    }

    public int getApplicationNumber() {
        int newApplicationNumber = 0;
        synchronized(applicationNumberLock) {
            HostelSetting hostelSetting = this.getHostelSetting(APPLICATION_NUMBER);
            String applicationNo = hostelSetting.getSettingValue();
            if (applicationNo != null) {
                applicationNumber = Integer.parseInt(applicationNo);
            }  
        
            newApplicationNumber = ++applicationNumber;
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            hostelSetting.setSettingValue(Integer.toString(applicationNumber));
            entityManager.merge(hostelSetting);
            transaction.commit();
        }        
        
        log.info("New application number: " + newApplicationNumber);
        return newApplicationNumber;
    }

    public void setApplicationNumber(int applicationNumber) {
        HostelSettingsHelper.applicationNumber = applicationNumber;
    }
    
    

    public String getHostelEmail() {
        if (hostelEmail == null) {
            hostelEmail = this.getHostelSettingByName(HOSTEL_EMAIL);
        }
        return hostelEmail;
    }

    public void setHostelEmail(String hostelEmail) {
        this.hostelEmail = hostelEmail;
    }

    public String getMedicalStudentEligibleMatricNo() {
        if (medicalStudentEligibleMatricNo == null) {
            medicalStudentEligibleMatricNo = this.getHostelSettingByName(MEDICALSTUDENT_ELIGIBLE_MATRICNO);
        }
        return medicalStudentEligibleMatricNo;
    }

    public void setMedicalStudentEligibleMatricNo(String medicalStudentEligibleMatricNo) {
        this.medicalStudentEligibleMatricNo = medicalStudentEligibleMatricNo;
    }

    public String getOtherStudentEligibleMatricNo() {
        if (otherStudentEligibleMatricNo == null) {
            otherStudentEligibleMatricNo = this.getHostelSettingByName(OTHERSTUDENT_ELIGIBLE_MATRICNO);
        }
        return otherStudentEligibleMatricNo;
    }

    public void setOtherStudentEligibleMatricNo(String otherStudentEligibleMatricNo) {
        this.otherStudentEligibleMatricNo = otherStudentEligibleMatricNo;
    }

    public Date getHostelBallotEndDate() {
        try {
            if(hostelBallotEndDate == null) {
                String endDate = getHostelSettingByName(HOSTEL_BALLOT_ENDDATE);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  //2012-06-10 21:40:22
                hostelBallotEndDate = dateFormat.parse(endDate);
            }
        
        } catch (Exception e) {
            log.error("Exception in getHosteBallotEndDate: " + e.getMessage());
        }
        return hostelBallotEndDate;
    }

    public void setHostelBallotEndDate(Date hosteBallotEndDate) {
        this.hostelBallotEndDate = hosteBallotEndDate;
    }

    public Date getHostelApplicationEndDate() {
        try {
            if(hostelApplicationEndDate == null) {
                String endDate = getHostelSettingByName(HOSTEL_APPLICATION_ENDDATE);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  //2012-06-10 21:40:22
                hostelApplicationEndDate = dateFormat.parse(endDate);
            }
        
        } catch (Exception e) {
            log.error("Exception in getHostelApplicationEndDate: " + e.getMessage());
        }
        return hostelApplicationEndDate;
    }

    public void setHostelApplicationEndDate(Date hostelApplicationEndDate) {
        this.hostelApplicationEndDate = hostelApplicationEndDate;
    }
    
    @PreDestroy
    public void destroyBean() {
        entityManager.close();        
    }
      
    
}
