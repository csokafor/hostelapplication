/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.perblo.hostel.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 *
 * @author chinedu
 */
@Entity
public class UnpaidHostelAllocation implements Serializable {
    
    private Long id;
        
    private String studentNumber;
    private HostelRoom hostelRoom;
    private HostelRoomBedSpace hostelRoomBedSpace;
    private AcademicSession academicSession;

    @ManyToOne
    public AcademicSession getAcademicSession() {
        return academicSession;
    }

    public void setAcademicSession(AcademicSession academicSession) {
        this.academicSession = academicSession;
    }

    @ManyToOne
    public HostelRoom getHostelRoom() {
        return hostelRoom;
    }

    public void setHostelRoom(HostelRoom hostelRoom) {
        this.hostelRoom = hostelRoom;
    }

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    @ManyToOne
    public HostelRoomBedSpace getHostelRoomBedSpace() {
        return hostelRoomBedSpace;
    }

    public void setHostelRoomBedSpace(HostelRoomBedSpace hostelRoomBedSpace) {
        this.hostelRoomBedSpace = hostelRoomBedSpace;
    }
    
    
    
}
