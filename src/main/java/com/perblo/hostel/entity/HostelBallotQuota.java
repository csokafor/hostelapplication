package com.perblo.hostel.entity;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Column;
import javax.persistence.Lob;
import javax.persistence.OneToOne;
import javax.persistence.Version;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@NamedQueries({
    @NamedQuery(name = "getHostelBallotQuotaByDepartment",
        query = "select object(t) from HostelBallotQuota t where t.department.id = ?1"),
    @NamedQuery(name = "getAllHostelBallotQuota",
        query = "select object(t) from HostelBallotQuota t")
})
public class HostelBallotQuota implements Serializable {

    // seam-gen attributes (you should probably edit these)
    private Long id;    
    private Integer numberOfMaleStudents;
    private Integer numberOfFemaleStudents;
    private Integer usedMaleQuota;
    private Integer usedFemaleQuota;    
    private Department department;
    //private ProgrammeOfStudy programmeOfStudy;
        
    

    // seam-gen attribute getters/setters with annotations (you probably should edit)
    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
            
    public Integer getNumberOfFemaleStudents() {
        return numberOfFemaleStudents;
    }

    public void setNumberOfFemaleStudents(Integer numberOfFemaleStudents) {
        this.numberOfFemaleStudents = numberOfFemaleStudents;
    }

    public Integer getNumberOfMaleStudents() {
        return numberOfMaleStudents;
    }

    public void setNumberOfMaleStudents(Integer numberOfMaleStudents) {
        this.numberOfMaleStudents = numberOfMaleStudents;
    }

    public Integer getUsedFemaleQuota() {
        return usedFemaleQuota;
    }

    public void setUsedFemaleQuota(Integer usedFemaleQuota) {
        this.usedFemaleQuota = usedFemaleQuota;
    }

    public Integer getUsedMaleQuota() {
        return usedMaleQuota;
    }

    public void setUsedMaleQuota(Integer usedMaleQuota) {
        this.usedMaleQuota = usedMaleQuota;
    }
          
    public void setDepartment(Department department) {
        this.department = department;
    }

    @ManyToOne
    @JoinColumn(name = "department_fk")    
    public Department getDepartment() {
        return department;
    }
      
    
}
