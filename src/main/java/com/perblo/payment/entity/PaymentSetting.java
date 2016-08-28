
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
		name="getPaymentSettingByName",
		query="select object(p) from PaymentSetting p where p.name = ?1"
	)
})
public class PaymentSetting implements Serializable {

	private Long id;    
    private Integer version;
    private String name;
    private String value;
    private String description;
  
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
    
    
    public String getName()
    {
        return this.name;
    }
    
    public void setName(String newValue)
    {
        this.name = newValue;
    }
    
    @Column(nullable=false)
    public String getValue()
    {
        return this.value;
    }
    
    public void setValue(String newValue)
    {
        this.value = newValue;
    }



    public String getDescription()
    {
        return this.description;
    }
    
    public void setDescription(String newValue)
    {
        this.description = newValue;
    }



}
