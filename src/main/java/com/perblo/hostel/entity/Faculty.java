package com.perblo.hostel.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity
@NamedQueries({
	@NamedQuery(
		name="getAllFaculties",
		query="select object(f) from Faculty as f"
	),
	@NamedQuery(
			name="getFacultyByName",
			query="select object(f) from Faculty f where f.name = ?1"
		)
})
public class Faculty implements Serializable {

    // seam-gen attributes (you should probably edit these)
    private Long id;    
    
    private HostelApplication transcriptRequest;
            
    private String code;
       
    private String name;
    
    private Set<Department> departments = new HashSet<Department>();
          
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

	public void setDepartments(Set<Department> departments) {
		this.departments = departments;
	}

	@OneToMany(mappedBy="faculty", cascade=CascadeType.REMOVE)
    @OrderBy("id")
	public Set<Department> getDepartments() {
		return departments;
	}	    
	
}
