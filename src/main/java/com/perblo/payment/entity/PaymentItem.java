
package com.perblo.payment.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Version;
import javax.persistence.Transient;


@Entity

@NamedQueries({
	@NamedQuery(
		name="getPaymentItemByNameAndPolicy",
		query="select object(p) from PaymentItem p where p.name = ?1"
	)
})
public class PaymentItem implements java.io.Serializable {
    
	private Long id;    
    private Integer version;
    private String name;    
    private String description;
    private double amount;
       
    public void setId(Long id) {
		this.id = id;
	}

    @Id @GeneratedValue
	public Long getId() {
		return id;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	@Version
	public Integer getVersion() {
		return version;
	}

	public java.lang.String getName() {
        return this.name;
    }
        
    public void setName(java.lang.String name) {
        this.name = name;
    }
	
	public void setAmount(double amount) {
		this.amount = amount;
	}

	@Transient
	public double getAmount() {
		return amount;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}          
        
}
