/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.perblo.hostel.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


/**
 *
 * @author chinedokaf
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "getEligibleStudentByStudentNumber",
        query = "select object(es) from EligibleStudent es where es.studentNumber = ?1")   
})
public class EligibleStudent implements Serializable {
    
    private Long id;    
    
    private String studentNumber;
   
    private String firstName;
            
    private String lastName;
            
    private String department;
    
    private String programmeOfStudy;
    
    private Integer yearOfStudy;
    
    private String academicSession;
    
    private HostelStudentType hostelStudentType;
    
    private Date dateUploaded;
    
    private String uploadedBy;

    public String getAcademicSession() {
        return academicSession;
    }

    public void setAcademicSession(String academicSession) {
        this.academicSession = academicSession;
    }
    
    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    public Date getDateUploaded() {
        return dateUploaded;
    }

    public void setDateUploaded(Date dateUploaded) {
        this.dateUploaded = dateUploaded;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getProgrammeOfStudy() {
        return programmeOfStudy;
    }

    public void setProgrammeOfStudy(String programmeOfStudy) {
        this.programmeOfStudy = programmeOfStudy;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public Integer getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(Integer yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }

    @ManyToOne
    public HostelStudentType getHostelStudentType() {
        return hostelStudentType;
    }

    public void setHostelStudentType(HostelStudentType hostelStudentType) {
        this.hostelStudentType = hostelStudentType;
    }
    
    
    
}
