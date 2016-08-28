package com.perblo.hostel.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Version;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@NamedQueries({
	@NamedQuery(
		name="getAllCourses",
		query="select object(c) from Course as c"
	)
})
public class Course implements Serializable {

    // seam-gen attributes (you should probably edit these)
    private Long id;    
            
    private String courseCode;
    
    private String MISC;
        
    private Department department;
    
    private Set<CourseTitle> courseTitles = new HashSet<CourseTitle>();
        
    // seam-gen attribute getters/setters with annotations (you probably should edit)

    @Id @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }    

	public void setCourseCode(String courseCode) {
		this.courseCode = courseCode;
	}

	@Column(nullable=false)
	public String getCourseCode() {
		return courseCode;
	}
		
	public void setDepartment(Department department) {
		this.department = department;
	}

	@ManyToOne
	@JoinColumn(name="department_fk")	
	public Department getDepartment() {
		return department;
	}

	public void setCourseTitles(Set<CourseTitle> courseTitles) {
		this.courseTitles = courseTitles;
	}

	@OneToMany(mappedBy="course", cascade=CascadeType.REMOVE)
    @OrderBy("id")
	public Set<CourseTitle> getCourseTitles() {
		return courseTitles;
	}

	public void setMISC(String mISC) {
		MISC = mISC;
	}

	public String getMISC() {
		return MISC;
	}	
	
	public String toString() {
		
		return "Course(id=" + this.id + ",courseCode=" + this.courseCode + ")";
		
	}
		
}
