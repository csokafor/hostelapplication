package com.perblo.hostel.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.FetchType;

@Entity
public class Tab implements Serializable {
	
	private Long id;
	
	private String tabName;
	
	private int tabOrder;
	
	private String description;
	
	private Tab parentTab;
	
	private boolean active;
	
	private Set<Role> tabRoles = new HashSet<Role>();
	
	private Set<Tab> subTabs = new HashSet<Tab>();

	public void setId(Long id) {
		this.id = id;
	}

	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	
	public void setTabName(String tabName) {
		this.tabName = tabName;
	}

	public String getTabName() {
		return tabName;
	}

	public void setTabOrder(int tabOrder) {
		this.tabOrder = tabOrder;
	}

	public int getTabOrder() {
		return tabOrder;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	@ManyToOne
    @JoinColumn(name="parentTab_fk")
    public Tab getParentTab() {
        return parentTab;
    }

    public void setParentTab(Tab parentTab) {
        this.parentTab = parentTab;
    }

    @OneToMany(mappedBy="parentTab")
    @OrderBy("tabOrder")
    @Basic(fetch=FetchType.EAGER)
    public Set<Tab> getSubTabs() {
        return subTabs;
    }

    public void setSubTabs(Set<Tab> subTabs) {
        this.subTabs = subTabs;
    }


	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isActive() {
		return active;
	}

	public void setTabRoles(Set<Role> tabRoles) {
		this.tabRoles = tabRoles;
	}

	@ManyToMany
	@JoinTable(name="TabRoles",
			joinColumns=
			@JoinColumn(name="tabId",
			referencedColumnName="id"),
			inverseJoinColumns=
			@JoinColumn(name="roleId",
			referencedColumnName="id"))
	public Set<Role> getTabRoles() {
		return tabRoles;
	}
	
	
	
}
