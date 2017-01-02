package com.perblo.hostel.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="Users")
@NamedQueries({
	@NamedQuery(
		name="getUserByUsername",
		query="select object(u) from User u where u.userName = ?1"
	),
	@NamedQuery(
		name="getUsersByRole",
		query="select object(u) from User u, Role r where r.roleName = ?1 and r MEMBER OF u.userRoles"
	)
})
public class User implements Serializable {

    // seam-gen attributes (you should probably edit these)
    private Long id;
       
    private String userName;
        
    private String userPassword;
        
    private String firstName;
       
    private String lastName;
    
    private String email;
    
    private String title;
    
    private String sex;
    
    private Date dateOfBirth;
    
    private String mobilePhone;
    
    private String officePhone;
    
    private int authFailureCount;
    
    private boolean accountBlocked;
    
    private Set<Role> userRoles = new HashSet<Role>();
        
        
    // seam-gen attribute getters/setters with annotations (you probably should edit)

    @Id @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
   	
	public void setUserName(String userName) {
		this.userName = userName;
	}

	@Column(nullable=false)
	public String getUserName() {
		return userName;
	}

	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}

	@Column(nullable=false)
	public String getUserPassword() {
		return userPassword;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@Column(nullable=false)
	public String getFirstName() {
		return firstName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@Column(nullable=false)
	public String getLastName() {
		return lastName;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Column(nullable=false)
	public String getEmail() {
		return email;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	@Column(nullable=false)
	public String getTitle() {
		return title;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public String getSex() {
		return sex;
	}

	public void setDateOfBirth(Date dateOfBirth) {
		this.dateOfBirth = dateOfBirth;
	}

	@Basic 
	@Temporal(TemporalType.DATE)
	public Date getDateOfBirth() {
		return dateOfBirth;
	}

	public void setAuthFailureCount(int authFailureCount) {
		this.authFailureCount = authFailureCount;
	}

	public int getAuthFailureCount() {
		return authFailureCount;
	}

	public void setAccountBlocked(boolean accountBlocked) {
		this.accountBlocked = accountBlocked;
	}

	public boolean isAccountBlocked() {
		return accountBlocked;
	}

	public void setUserRoles(Set<Role> userRoles) {
		this.userRoles = userRoles;
	}

	@ManyToMany(fetch=FetchType.EAGER)
	@JoinTable(name="UserRoles",
			joinColumns=@JoinColumn(name="userId",referencedColumnName="id"),
			inverseJoinColumns=@JoinColumn(name="roleId",referencedColumnName="id"))
	public Set<Role> getUserRoles() {
		return userRoles;
	}

	public void setMobilePhone(String mobilePhone) {
		this.mobilePhone = mobilePhone;
	}

	public String getMobilePhone() {
		return mobilePhone;
	}

	public void setOfficePhone(String officePhone) {
		this.officePhone = officePhone;
	}

	public String getOfficePhone() {
		return officePhone;
	}
	
	@Override
    public String toString() {		
		return "User(id=" + this.id + ",userName=" + this.userName + ")";		
	}
	
}
