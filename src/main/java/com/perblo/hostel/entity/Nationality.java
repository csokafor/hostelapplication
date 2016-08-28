package com.perblo.hostel.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;


@Entity

@NamedQueries({
	@NamedQuery(
		name="getAllNationalities",
		query="select object(n) from Nationality as n"
	)
})
public class Nationality  implements Serializable {

	private Long id;
       
    private String code;
       
    private String name;
    
    public Nationality() {
    	
    }
    
              
    // seam-gen attribute getters/setters with annotations (you probably should edit)

    @Id @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
   	
	public void setCode(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
