package com.perblo.payment.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Version;

@Entity
@NamedQueries({
    @NamedQuery(name = "getPinByPinNumber",
    query = "select object(p) from Pin p where p.pinNumber = ?1"),
    @NamedQuery(name = "getPinBySerialNumber",
    query = "select object(p) from Pin p where p.serialNumber = ?1")
})
public class Pin implements Serializable {

    private Long id;
    private Integer version;
    private float pinValue;
    private String pinNumber;
    private String batchNumber;
    private Date generationDate;
    private Date dateUsed;
    private boolean usedStatus;
    private boolean enabled;
    private String serialNumber;
    private Date dateUploaded;
    private String uploadedBy;

    @Id
    @GeneratedValue
    public Long getId() {
        return this.id;
    }

    public void setId(Long newValue) {
        this.id = newValue;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Version
    public Integer getVersion() {
        return version;
    }

    public void setPinValue(float pinValue) {
        this.pinValue = pinValue;
    }

    @Column(nullable = false)
    public float getPinValue() {
        return pinValue;
    }

    public void setPinNumber(String pinNumber) {
        this.pinNumber = pinNumber;
    }

    @Column(nullable = false, unique = true)
    public String getPinNumber() {
        return pinNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setGenerationDate(Date generationDate) {
        this.generationDate = generationDate;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getGenerationDate() {
        return generationDate;
    }

    public void setDateUsed(Date dateUsed) {
        this.dateUsed = dateUsed;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getDateUsed() {
        return dateUsed;
    }

    public void setUsedStatus(boolean usedStatus) {
        this.usedStatus = usedStatus;
    }

    public boolean isUsedStatus() {
        return usedStatus;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    @Temporal(TemporalType.TIMESTAMP)
    public Date getDateUploaded() {
        return dateUploaded;
    }

    public void setDateUploaded(Date dateUploaded) {
        this.dateUploaded = dateUploaded;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
}
