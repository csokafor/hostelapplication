package com.perblo.hostel.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.OneToMany;
import javax.persistence.Column;
import javax.persistence.Version;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;



@Entity

@NamedQueries({	
	@NamedQuery(
			name="getStudentByStudentNumber",
			query="select object(s) from Student s where s.studentNumber = ?1"
		)
})
public class Student implements Serializable {

    // seam-gen attributes (you should probably edit these)
    private Long id;
    private Integer version;
   
    private String studentNumber;
   
    private String email;
    
    private String firstName;
    
    private String otherNames;
      
    private String lastName;
   
    private String gender;
        
    private Date dateOfBirth;
       
    private String nationality;
               
    private String yearOfAdmission;
       
    private String modeOfEntry;
       
    private String yearOfGraduation;
    
    private Faculty faculty;
    
    private Department department;
    
    private ProgrammeOfStudy programmeOfStudy;
    
    //private Transcript transcript;
    
    
    // seam-gen attribute getters/setters with annotations (you probably should edit)

    @Id @GeneratedValue
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

	public void setNationality(String nationality) {
		this.nationality = nationality;
	}

	public String getNationality() {
		return nationality;
	}
		
	public void setYearOfGraduation(String yearOfGraduation) {
		this.yearOfGraduation = yearOfGraduation;
	}

	public String getYearOfGraduation() {
		return yearOfGraduation;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public String getGender() {
		return gender;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	
	@Basic @Temporal(TemporalType.DATE)	
	public Date getDateOfBirth() {
		return dateOfBirth;
	}
	
	public void setFaculty(Faculty faculty) {
		this.faculty = faculty;
	}

	@ManyToOne
	@JoinColumn(name="faculty_fk")	
	public Faculty getFaculty() {
		return faculty;
	}

	public void setDepartment(Department department) {
		this.department = department;
	}

	@ManyToOne
	@JoinColumn(name="department_fk")	
	public Department getDepartment() {
		return department;
	}

	public void setProgrammeOfStudy(ProgrammeOfStudy programmeOfStudy) {
		this.programmeOfStudy = programmeOfStudy;
	}

   
	public void setYearOfAdmission(String yearOfAdmission) {
		this.yearOfAdmission = yearOfAdmission;
	}

	public String getYearOfAdmission() {
		return yearOfAdmission;
	}

	public void setModeOfEntry(String modeOfEntry) {
		this.modeOfEntry = modeOfEntry;
	}

	public String getModeOfEntry() {
		return modeOfEntry;
	}
	
	@Override
    public String toString() {		
		return "Student(id=" + this.id + ",studentNumber=" + this.studentNumber + ")";		
	}
	
}
