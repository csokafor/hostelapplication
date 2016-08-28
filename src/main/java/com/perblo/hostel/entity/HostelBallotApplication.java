package com.perblo.hostel.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Version;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@NamedQueries({
    @NamedQuery(name = "getHostelBallotApplicationByApplicationNumber",
        query = "select object(t) from HostelBallotApplication t where t.applicationNumber = ?1"),
    @NamedQuery(name = "getHostelBallotApplicationByStudentNumber",
        query = "select object(t) from HostelBallotApplication t where t.studentNumber = ?1"),
    @NamedQuery(name = "getHostelBallotApplicationByStudentNumberAndApplicationNumber",
        query = "select object(t) from HostelBallotApplication t where t.studentNumber = ?1 and t.applicationNumber = ?2"),
    @NamedQuery(name = "getHostelBallotApplicationByAcademicSessionAndStudentNumber",
        query = "select object(t) from HostelBallotApplication t where t.academicSession.id = ?1 and t.studentNumber = ?2")   
})
public class HostelBallotApplication implements Serializable {

    // seam-gen attributes (you should probably edit these)
    private Long id;
       
    private String applicationNumber;    
    private String studentNumber;
    private String gender;
    private String email;
    private String phoneNumber;
   
    private String firstName;    
    private String otherNames;    
    private String lastName;
    private Faculty faculty;
    private Department department;
    private ProgrammeOfStudy programmeOfStudy;
        
    private String yearOfStudy;
    private Date applicationDate;
    private int applicationStatus;
    private int ballotStatus;
        
    private HostelAllocation hostelAllocation;
    private AcademicSession academicSession;
    private HostelStudentType studentType;
    
    private byte[] passportData;        
    //private String passportFileExtension;
    //private String passportFileName;    
    private String passportContentType;

    // seam-gen attribute getters/setters with annotations (you probably should edit)
    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
   
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setOtherNames(String otherNames) {
        this.otherNames = otherNames;
    }

    public String getOtherNames() {
        return otherNames;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public String getYearOfStudy() {
        return yearOfStudy;
    }

    public void setYearOfStudy(String yearOfStudy) {
        this.yearOfStudy = yearOfStudy;
    }

    @OneToOne
    public HostelAllocation getHostelAllocation() {
        return hostelAllocation;
    }

    public void setHostelAllocation(HostelAllocation hostelAllocation) {
        this.hostelAllocation = hostelAllocation;
    }
    
    
    @ManyToOne
    public AcademicSession getAcademicSession() {
        return academicSession;
    }

    public void setAcademicSession(AcademicSession academicSession) {
        this.academicSession = academicSession;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }        
    
    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    public Date getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(Date applicationDate) {
        this.applicationDate = applicationDate;
    }

    public int getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(int applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }    
    
    public int getBallotStatus() {
        return ballotStatus;
    }

    public void setBallotStatus(int ballotStatus) {
        this.ballotStatus = ballotStatus;
    }
        
  
    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    @ManyToOne
    @JoinColumn(name = "faculty_fk")    
    public Faculty getFaculty() {
        return faculty;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    @ManyToOne
    @JoinColumn(name = "department_fk")    
    public Department getDepartment() {
        return department;
    }

    public void setProgrammeOfStudy(ProgrammeOfStudy programmeOfStudy) {
        this.programmeOfStudy = programmeOfStudy;
    }

    @ManyToOne
    @JoinColumn(name = "programmeOfStudy_fk")    
    public ProgrammeOfStudy getProgrammeOfStudy() {
        return programmeOfStudy;
    }

    public String getPassportContentType() {
        return passportContentType;
    }

    public void setPassportContentType(String passportContentType) {
        this.passportContentType = passportContentType;
    }

    @Lob
    public byte[] getPassportData() {
        return passportData;
    }
    
    public void setPassportData(byte[] passportData) {
        this.passportData = passportData;
    }
    
    

    /*
    public String getPassportFileExtension() {
        return passportFileExtension;
    }

    public void setPassportFileExtension(String passportFileExtension) {
        this.passportFileExtension = passportFileExtension;
    }

    public String getPassportFileName() {
        return passportFileName;
    }

    public void setPassportFileName(String passportFileName) {
        this.passportFileName = passportFileName;
    }
    */

    @ManyToOne
    public HostelStudentType getStudentType() {
        return studentType;
    }

    public void setStudentType(HostelStudentType studentType) {
        this.studentType = studentType;
    }

    @Override
    public String toString() {
        return "HostelBallotApplication{" + "id=" + id + ", applicationNumber=" + applicationNumber + ", studentNumber=" + 
                studentNumber + ", firstName=" + firstName + ", lastName=" + lastName + 
                ", applicationDate=" + applicationDate + ", applicationStatus=" + applicationStatus + ", ballotStatus=" + ballotStatus + '}';
    }
    
    
    
}
