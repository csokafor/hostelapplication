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
		name="getAllCourseTitles",
		query="select object(c) from Course as c"
	),
	@NamedQuery(
		name="getCourseTitleByCourseCode",
		query="select object(ct) from CourseTitle ct where ct.course.courseCode = ?1"
	),
	@NamedQuery(
		name="getCourseTitleByCourseId",
		query="select object(ct) from CourseTitle ct where ct.course.id = ?1"
	),
	@NamedQuery(
			name="getCourseTitleByCourseTitleAndCode",
			query="select object(ct) from CourseTitle ct where ct.courseTitle = ?1 and ct.course.courseCode = ?2"
		)
})
public class CourseTitle implements Serializable {

    // seam-gen attributes (you should probably edit these)
    private Long id;
           
    private String courseTitle;
    
    private Course course;
    
    //private Set<TranscriptCourse> transcriptCourses = new HashSet<TranscriptCourse>();
        
    // seam-gen attribute getters/setters with annotations (you probably should edit)

    @Id @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
	
	public void setCourseTitle(String courseTitle) {
		this.courseTitle = courseTitle;
	}

	@Column(nullable=false)
	public String getCourseTitle() {
		return courseTitle;
	}

	
	public void setCourse(Course course) {
		this.course = course;
	}

	@ManyToOne
	@JoinColumn(name="course_fk")	
	public Course getCourse() {
		return course;
	}	
	
	@Override
    public boolean equals(Object anotherObject){
        boolean reply = false;
        try{
            CourseTitle anotherCourseTitle = (CourseTitle)anotherObject;
            reply = (this.getId().equals(anotherCourseTitle.getId()));
        }catch(Exception e){
            reply = false;
        }finally{
            return reply;
        }
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + (this.id != null ? this.id.hashCode() : 0);
        hash = 79 * hash + (this.courseTitle != null ? this.courseTitle.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {		
		return "CourseTitle(id=" + this.id + ",courseTitle=" + this.courseTitle + ")";		
	}
		
}
