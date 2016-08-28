/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.perblo.hostel.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;

/**
 *
 * @author chinedu
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "getAllHostels",
        query = "select object(h) from Hostel h"),
    @NamedQuery(name = "getHostelByGender",
        query = "select object(h) from Hostel h where h.gender = ?1"),
    @NamedQuery(name = "getHostelByName",
        query = "select object(h) from Hostel h where h.hostelName = ?1")
})
public class Hostel implements Serializable {
    
    private Integer id;
        
    private String hostelName;
    private String description;
    private String gender;
    private Integer numberOfRooms;
    private Set<HostelRoom> hostelRooms = new HashSet<HostelRoom>();

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getHostelName() {
        return hostelName;
    }

    public void setHostelName(String hostelName) {
        this.hostelName = hostelName;
    }

    @OneToMany(mappedBy="hostel")
    @OrderBy("roomNumber ASC")
    public Set<HostelRoom> getHostelRooms() {
        return hostelRooms;
    }

    public void setHostelRooms(Set<HostelRoom> hostelRooms) {
        this.hostelRooms = hostelRooms;
    }

    @Id
    @GeneratedValue
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getNumberOfRooms() {
        return numberOfRooms;
    }

    public void setNumberOfRooms(Integer numberOfRooms) {
        this.numberOfRooms = numberOfRooms;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    
    
    
}
