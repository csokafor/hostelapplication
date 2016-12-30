package com.perblo.hostel.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Version;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


@Entity
@NamedQueries({
    @NamedQuery(
        name = "getAllDepartments",
        query = "select object(d) from Department d"
    ),
    @NamedQuery(
        name = "getDepartmentByFacultyId",
        query = "select object(d) from Department d where d.faculty.id = ?1"
    ),
    @NamedQuery(
            name = "getDepartmentByName",
            query = "select object(d) from Department d where d.name = ?1"
    )
})

public class Department implements Serializable {

    // seam-gen attributes (you should probably edit these)
    private Long id;

    private Faculty faculty;

    private String code;

    private String name;

    private Set<ProgrammeOfStudy> programmeOfStudy = new HashSet<ProgrammeOfStudy>();

    // seam-gen attribute getters/setters with annotations (you probably should edit)

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setFaculty(Faculty faculty) {
        this.faculty = faculty;
    }

    @ManyToOne
    @JoinColumn(name = "faculty_fk")
    public Faculty getFaculty() {
        return faculty;
    }

    public void setProgrammeOfStudy(Set<ProgrammeOfStudy> programmeOfStudy) {
        this.programmeOfStudy = programmeOfStudy;
    }

    @OneToMany(mappedBy = "department", cascade = CascadeType.REMOVE)
    @OrderBy("id")
    public Set<ProgrammeOfStudy> getProgrammeOfStudy() {
        return programmeOfStudy;
    }

}
