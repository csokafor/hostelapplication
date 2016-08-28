package com.perblo.hostel.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Version;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@NamedQueries({
	@NamedQuery(
		name="getAllSemesters",
		query="select object(s) from Semester as s"
	)
})
public class Semester implements Serializable {

    // seam-gen attributes (you should probably edit these)
    private Long id;
   
    private String semesterName;
                
    // seam-gen attribute getters/setters with annotations (you probably should edit)

    @Id @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
  
	public void setSemesterName(String semesterName) {
		this.semesterName = semesterName;
	}

	public String getSemesterName() {
		return semesterName;
	}
	 
	
}
