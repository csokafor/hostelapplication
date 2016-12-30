package com.perblo.hostel.service;

import com.perblo.hostel.entity.AcademicSession;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedProperty;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.perblo.hostel.entitymanager.HostelEntityManager;
import com.perblo.hostel.entitymanager.HostelEntityManagerImpl;

import com.perblo.hostel.entity.HostelSetting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Map;
import javax.persistence.EntityTransaction;


@Service(value="hostelSettingsService")
@Transactional(value = "hostelTxManager", rollbackFor = Exception.class)
public class HostelSettingsService {
    private static final Logger log = LoggerFactory.getLogger(HostelSettingsService.class);

    public static String BALLOT_NUMBER = "BALLOT_NUMBER";
    public static String APPLICATION_NUMBER = "APPLICATION_NUMBER";
    private static int ballotNumber;
    private static int applicationNumber;

    @ManagedProperty(value="#{hostelConfig}")
    private HostelConfig hostelConfig;

    @Autowired
    HostelEntityManager hostelEntityManager;

    private String schoolName;

    private double accommodationFee;

    private AcademicSession currentAcademicSession;

    private String hostelEmail;

    private String otherStudentEligibleMatricNo;

    private String medicalStudentEligibleMatricNo;

    private Date hostelBallotEndDate;

    private Date hostelApplicationEndDate;

    private final Object ballotNumberLock = new Object();
    private final Object applicationNumberLock = new Object();

    public HostelSettingsService() {
    }

    @PostConstruct
    public void init() {
        int currentSessionId = Integer.parseInt(getHostelSettingByName("CURRENT_ACADEMICSESSION"));
        currentAcademicSession = hostelEntityManager.getEntityManager().find(AcademicSession.class, currentSessionId);

        schoolName = getHostelSettingByName("SCHOOL_NAME");
        accommodationFee = Double.parseDouble(getHostelSettingByName("ACCOMMODATION_FEE"));
    }

    public String getHostelSettingByName(String name) {
        String settingValue = null;
        Query query = hostelEntityManager.getEntityManager().createNamedQuery("getHostelSettingByName");
        query.setParameter(1, name);
        HostelSetting hostelSetting = (HostelSetting) query.getSingleResult();
        settingValue = hostelSetting.getSettingValue();

        return settingValue;
    }
    
    public HostelSetting getHostelSetting(String name) {
        HostelSetting settingValue = null;
        Query query = hostelEntityManager.getEntityManager().createNamedQuery("getHostelSettingByName");
        query.setParameter(1, name);
        settingValue = (HostelSetting) query.getSingleResult();

        return settingValue;
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
            hostelSetting.setSettingValue(Integer.toString(ballotNumber));
            hostelEntityManager.getEntityManager().merge(hostelSetting);
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
            hostelSetting.setSettingValue(Integer.toString(applicationNumber));
            hostelEntityManager.getEntityManager().merge(hostelSetting);
        }        
        
        log.info("New application number: " + newApplicationNumber);
        return newApplicationNumber;
    }

    public HostelConfig getHostelConfig() {
        return hostelConfig;
    }

    public void setHostelConfig(HostelConfig hostelConfig) {
        this.hostelConfig = hostelConfig;
    }

    public void setApplicationNumber(int applicationNumber) {
        HostelSettingsService.applicationNumber = applicationNumber;
    }

    public AcademicSession getCurrentAcademicSession() {
        return currentAcademicSession;
    }

    public void setCurrentAcademicSession(AcademicSession currentAcademicSession) {
        this.currentAcademicSession = currentAcademicSession;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public void setAccommodationFee(double accommodationFee) {
        this.accommodationFee = accommodationFee;
    }

    public double getAccommodationFee() {
        return accommodationFee;
    }

    public String getHostelEmail() {
        return hostelEmail;
    }

    public void setHostelEmail(String hostelEmail) {
        this.hostelEmail = hostelEmail;
    }

    public String getMedicalStudentEligibleMatricNo() {
        return medicalStudentEligibleMatricNo;
    }

    public void setMedicalStudentEligibleMatricNo(String medicalStudentEligibleMatricNo) {
        this.medicalStudentEligibleMatricNo = medicalStudentEligibleMatricNo;
    }

    public String getOtherStudentEligibleMatricNo() {
        return otherStudentEligibleMatricNo;
    }

    public void setOtherStudentEligibleMatricNo(String otherStudentEligibleMatricNo) {
        this.otherStudentEligibleMatricNo = otherStudentEligibleMatricNo;
    }

    public Date getHostelBallotEndDate() {

        try {
            if(hostelBallotEndDate == null) {
                String endDate = getHostelSettingByName("HOSTEL_BALLOT_ENDDATE");
                log.info("ballot end date " + endDate);
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
                String endDate = getHostelSettingByName("HOSTEL_APPLICATION_ENDDATE");
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
}
