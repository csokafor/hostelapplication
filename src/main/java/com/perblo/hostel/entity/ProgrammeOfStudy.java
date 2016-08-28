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
		name="getAllProgrammeOfStudy",
		query="select object(p) from ProgrammeOfStudy as p"
	),
	@NamedQuery(
			name="getProgrammeOfStudyByDepartmentId",
			query="select object(p) from ProgrammeOfStudy p where p.department.id = ?1"
		)
})
public class ProgrammeOfStudy implements Serializable {

    // seam-gen attributes (you should probably edit these)
    private Long id;
       
    private String code;
        
    private String name;
    
    private Department department;
        
        
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

	public void setDepartment(Department department) {
		this.department = department;
	}

	@ManyToOne
	@JoinColumn(name="department_fk")	
	public Department getDepartment() {
		return department;
	}
	
	@Override
    public boolean equals(Object anotherObject){
        boolean reply = false;
        try{
            ProgrammeOfStudy anotherProgrammeOfStudy = (ProgrammeOfStudy)anotherObject;
            reply = (this.getId().equals(anotherProgrammeOfStudy.getId()));
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
        hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
    
    @Override
    public String toString() {		
		return "ProgrammeOfStudy(id=" + this.id + ",code=" + this.code + ",name=" + this.name + ")";		
	}
	
}
