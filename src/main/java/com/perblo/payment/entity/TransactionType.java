
package com.perblo.payment.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Version;

@Entity

@NamedQueries({
	@NamedQuery(
		name="getEnabledTransactionTypes",
		query="select object(t) from TransactionType t where t.enabled = true"
	)
})
public class TransactionType implements Serializable {
	
    private Long id;    
    private Integer version;        
    private boolean enabled;
    private String name;
    private String description;
    private String imageUrl;
    private String paymentPolicyName;
    //private String paymentPolicyUrl;
          
  
   @Id @GeneratedValue
    public Long getId()
    {
        return this.id;
    }
    
    public void setId(Long newValue)
    {
        this.id = newValue;
    }

    public void setVersion(Integer version) {
		this.version = version;
	}

    @Version
	public Integer getVersion() {
		return version;
	}

    @Column(nullable=false)
	public String getName()
    {
        return this.name;
    }
    
    public void setName(String newValue)
    {
        this.name = newValue;
    }


    public String getDescription()
    {
        return this.description;
    }
    
    public void setDescription(String newValue)
    {
        this.description = newValue;
    }


    public String getImageUrl()
    {
        return this.imageUrl;
    }
    
    public void setImageUrl(String newValue)
    {
        this.imageUrl = newValue;
    }

    
    @Column(nullable=false)
    public String getPaymentPolicyName()
    {
        return this.paymentPolicyName;
    }
    
    public void setPaymentPolicyName(String newValue)
    {
        this.paymentPolicyName = newValue;
    }

 /*
    public String getPaymentPolicyUrl()
    {
        return this.paymentPolicyUrl;
    }
    
    public void setPaymentPolicyUrl(String newValue)
    {
        this.paymentPolicyUrl = newValue;
    }
*/
   
    @Column(nullable=false)
    public boolean isEnabled()
    {
        return this.enabled;
    }
    
    public void setEnabled(boolean newValue)
    {
        this.enabled = newValue;
    }




}
