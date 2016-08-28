
package com.perblo.payment.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;


@Entity

@NamedQueries({	
	@NamedQuery(
		name="getPaymentTransactionByTransactionId",
		query="select object(p) from PaymentTransaction p where p.transactionId = ?1"
	)
})
public class PaymentTransaction  implements Serializable {

	private Long id;    
    private Integer version;
   
    private String transactionId;
    private Date paymentDate;
    private String description;
    private String referenceId;
    private double amount;
    private int status;
    private String statusMessage;
    private int userId;
    
    private TransactionType transactionType;
    
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

	@Column(nullable=false)
	public String getTransactionId()
    {
        return this.transactionId;
    }
    
    public void setTransactionId(String newValue)
    {
        this.transactionId = newValue;
    }


    @Basic @Temporal(TemporalType.TIMESTAMP)
    public Date getPaymentDate()
    {
        return this.paymentDate;
    }
    
    public void setPaymentDate(Date newValue)
    {
        this.paymentDate = newValue;
    }


    public String getDescription()
    {
        return this.description;
    }
    
    public void setDescription(String newValue)
    {
        this.description = newValue;
    }


    public String getReferenceId()
    {
        return this.referenceId;
    }
    
    public void setReferenceId(String newValue)
    {
        this.referenceId = newValue;
    }


    @Column(nullable=false)
    public double getAmount()
    {
        return this.amount;
    }
    
    public void setAmount(double newValue)
    {
        this.amount = newValue;
    }

    @Column(nullable=false)
    public int getStatus()
    {
        return this.status;
    }
    
    public void setStatus(int newValue)
    {
        this.status = newValue;
    }
    
   
    public String getStatusMessage()
    {
        return this.statusMessage;
    }
    
    public void setStatusMessage(String newValue)
    {
        this.statusMessage = newValue;
    }

    @Column(nullable=false)
    public int getUserId()
    {
        return this.userId;
    }
    
    public void setUserId(int newValue)
    {
        this.userId = newValue;
    }

	public void setTransactionType(TransactionType transactionType) {
		this.transactionType = transactionType;
	}

	@ManyToOne
	@JoinColumn(name="transactionType_fk")
	public TransactionType getTransactionType() {
		return transactionType;
	}

 
}
