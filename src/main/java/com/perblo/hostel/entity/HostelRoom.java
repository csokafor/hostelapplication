/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.perblo.hostel.entity;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.persistence.*;

/**
 *
 * @author chinedu
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "getHostelRoomByHostel",
        query = "select object(h) from HostelRoom h where h.hostel.id = ?1 order by roomNumber"),
    @NamedQuery(name = "getHostelRoomByHostelAndRoomNumber",
        query = "select object(h) from HostelRoom h where h.hostel.id = ?1 and h.roomNumber = ?2")
})
public class HostelRoom implements Serializable {
    
    private Long id;
        
    private Integer numberOfOccupants;
    private String roomNumber;
    private Hostel hostel;
    private Set<HostelAllocation> hostelAllocations = new LinkedHashSet<HostelAllocation>();
    //private Set<HostelRoomBedSpace> allocatedHostelRoomBedSpace = new HashSet<HostelRoomBedSpace>();

    @ManyToOne
    public Hostel getHostel() {
        return hostel;
    }

    public void setHostel(Hostel hostel) {
        this.hostel = hostel;
    }

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getNumberOfOccupants() {
        return numberOfOccupants;
    }

    public void setNumberOfOccupants(Integer numberOfOccupants) {
        this.numberOfOccupants = numberOfOccupants;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    @OneToMany(fetch= FetchType.EAGER, mappedBy="hostelRoom")
    public Set<HostelAllocation> getHostelAllocations() {
        return hostelAllocations;
    }

    public void setHostelAllocations(Set<HostelAllocation> hostelAllocations) {
        this.hostelAllocations = hostelAllocations;
    }

    /*
    @ManyToMany
    public Set<HostelRoomBedSpace> getAllocatedHostelRoomBedSpace() {
        return allocatedHostelRoomBedSpace;
    }

    public void setAllocatedHostelRoomBedSpace(Set<HostelRoomBedSpace> allocatedHostelRoomBedSpace) {
        this.allocatedHostelRoomBedSpace = allocatedHostelRoomBedSpace;
    }
    */
    

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HostelRoom other = (HostelRoom) obj;
        if (this.roomNumber != other.roomNumber && (this.roomNumber == null || !this.roomNumber.equals(other.roomNumber))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + (this.roomNumber != null ? this.roomNumber.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return "HostelRoom{" + "id=" + id + ", numberOfOccupants=" + numberOfOccupants + ", roomNumber=" + roomNumber + '}';
    }
    
    
    
}
