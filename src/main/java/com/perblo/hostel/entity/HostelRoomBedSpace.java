/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.perblo.hostel.entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

/**
 *
 * @author chinedu
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "getAllHostelRoomBedSpace",
        query = "select object(h) from HostelRoomBedSpace h order by h.position"),
    @NamedQuery(name = "getFourRoomHostelRoomBedSpace",
        query = "select object(h) from HostelRoomBedSpace h where h.position not like '%center%' order by h.position"),
    @NamedQuery(name = "getHostelRoomBedSpaceByPosition",
        query = "select object(h) from HostelRoomBedSpace h where h.position = ?1")
})
public class HostelRoomBedSpace implements Serializable {
    
    private Integer id;
    private String position;

    @Id
    @GeneratedValue
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HostelRoomBedSpace other = (HostelRoomBedSpace) obj;
        if (this.id != other.id && (this.id == null || !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + (this.id != null ? this.id.hashCode() : 0);
        return hash;
    }

    
    
    
}
