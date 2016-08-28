package com.perblo.hostel.entity;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Version;

@Entity
@NamedQueries({
    @NamedQuery(name = "getHostelSettingByName",
    query = "select object(t) from HostelSetting t where t.name = ?1")
})
public class HostelSetting implements Serializable {

    // seam-gen attributes (you should probably edit these)
    private Long id;
       
    private String name;
   
    private String settingValue;
    
    private String description;

    // seam-gen attribute getters/setters with annotations (you probably should edit)
    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }
   
    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
