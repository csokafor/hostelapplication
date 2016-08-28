package com.perblo.hostel.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.ManyToMany;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Version;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity

@NamedQueries({
	@NamedQuery(
		name="getAllRoles",
		query="select object(r) from Role as r"
	)
})
public class Role implements Serializable {

    // seam-gen attributes (you should probably edit these)
    private Long id;
   
    private String roleName;
        
    private Set<User> users = new HashSet<User>();
    
    private Set<Tab> tabs = new HashSet<Tab>();
        
    // seam-gen attribute getters/setters with annotations (you probably should edit)

    @Id @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
   
	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setUsers(Set<User> users) {
		this.users = users;
	}

	@ManyToMany(mappedBy="userRoles")
	public Set<User> getUsers() {
		return users;
	}

	public void setTabs(Set<Tab> tabs) {
		this.tabs = tabs;
	}

	@ManyToMany(mappedBy="tabRoles")
	public Set<Tab> getTabs() {
		return tabs;
	}
	
	@Override
    public boolean equals(Object anotherObject){
        boolean reply = false;
        try{
            Role anotherRole = (Role)anotherObject;
            reply = (this.getId().equals(anotherRole.getId()) && this.getRoleName().equals(anotherRole.getRoleName()));
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
        hash = 79 * hash + (this.roleName != null ? this.roleName.hashCode() : 0);
        return hash;
    }
	
    @Override
    public String toString() {		
		return "Role(id=" + this.id + ",roleName=" + this.roleName + ")";		
	}
		
}
