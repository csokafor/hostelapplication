package com.perblo.hostel.service;

import com.perblo.hostel.entity.AcademicSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.Date;

/**
 * Created by cokafor on 12/24/2016.
 */
@Configuration
@PropertySource("classpath:hostel.properties")
public class HostelConfig {
    /*
    @Value("${school.name}")
    private String schoolName;

    @Value("${accommodation.fee}")
    private double accommodationFee;

    @Value("${current.academicsession}")
    private String currentAcademicSession;

    @Value("${hostel.email}")
    private String hostelEmail;

    @Value("${otherstudent.matricnumber}")
    private String otherStudentEligibleMatricNo;

    @Value("${medicalstudent.matricnumber}")
    private String medicalStudentEligibleMatricNo;

    @Value("${hostel.ballot.enddate}")
    private Date hostelBallotEndDate;

    @Value("${hostel.application.enddate}")
    private Date hostelApplicationEndDate;

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

    public String getCurrentAcademicSession() {
        return currentAcademicSession;
    }

    public void setCurrentAcademicSession(String currentAcademicSession) {
        this.currentAcademicSession = currentAcademicSession;
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

    */

}
