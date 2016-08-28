package com.perblo.hostel.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity

@NamedQueries({
	@NamedQuery(
		name="getAllSchoolYear",
		query="select object(y) from SchoolYear y order by y.year"
	)
})
public class SchoolYear  implements Serializable {

	private Long id;
           
    private String year;
    
    @Id @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
   
	public void setYear(String year) {
		this.year = year;
	}

	public String getYear() {
		return year;
	}
	
	public String toString() {
		return this.year;
	}
}
