/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.perblo.hostel.entity;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 * @author chinedu
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "getHostelStudentTypeByStudentType",
        query = "select object(hs) from HostelStudentType hs where hs.studentType = ?1")   
})
public class HostelStudentType implements Serializable {
    
    /*
     * A. Eligible ( for students who picked "yes" on ballot)
     B. Staff/ special request
     C. Sports
     D. Handicap student.
     E. Year One/ Direct entry

     */
    
    private Long id;    
    private String code;
    private String studentType;
    private double hostelFee;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentType() {
        return studentType;
    }

    public void setStudentType(String studentType) {
        this.studentType = studentType;
    }

    public double getHostelFee() {
        return hostelFee;
    }

    public void setHostelFee(double hostelFee) {
        this.hostelFee = hostelFee;
    }
    
    
}
