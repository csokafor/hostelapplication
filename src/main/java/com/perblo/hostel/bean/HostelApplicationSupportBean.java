/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.perblo.hostel.bean;

import com.perblo.hostel.entity.*;
import com.perblo.hostel.entitymanager.HostelEntityManager;
import com.perblo.hostel.entitymanager.HostelEntityManagerImpl;
import com.perblo.hostel.service.HostelApplicationService;
import com.perblo.hostel.service.HostelSettingsService;

import com.perblo.security.LoginUserBean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;


/**
 *
 * @author chinedu
 */

@ManagedBean(name="hostelAppSupportBean")
@SessionScoped
@Secured("Hostel Application Admin")
public class HostelApplicationSupportBean implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(HostelApplicationSupportBean.class);

    @ManagedProperty(value = "#{hostelEntityManager}")
    HostelEntityManager hostelEntityManager;

    @ManagedProperty(value="#{hostelApplicationService}")
    private HostelApplicationService hostelApplicationService;
         
    @ManagedProperty(value="#{hostelSettingsService}")
    private HostelSettingsService hostelSettingsService;
    
    @ManagedProperty(value="#{loginUserBean}")
    LoginUserBean loginUserBean;
    
    private int pageSize = 20;    
    private int page;        
    private String searchString;    
    private String newStudentNumber;    
    private String hostelStudentNumber;    
    private String newStudentType;    
    
    private String studentNumber;
    private String studentName;
    private String studentType;
    private long departmentId;
    private long facultyId;
    
    private Hostel hostel;    
    private HostelRoom hostelRoom;    
    private HostelRoomBedSpace roomBedSpace;    
    private List<HostelRoom> hostelRooms = new ArrayList<HostelRoom>();
    
    private int hostelId;
    private long hostelRoomId;
    private int roomBedSpaceId;
        
    private EligibleStudent eligibleStudent;        
    private HostelAllocation hostelAllocation;    
    private List<EligibleStudent> eligibleStudents;
    private List<Department> departments = new ArrayList<Department>();
       
    public HostelApplicationSupportBean() {

    }
           
    public void search() {
       page = 0;
        studentNumber = "";
        studentName = "";
        studentType = "";
        facultyId = 0;
        departmentId = 0;
        hostelId = 0;
        hostelRoomId = 0;
        roomBedSpaceId = 0;

       findEligibleStudents();
    }
    
    public void updateStudentNumber() {
        log.info("newStudentNumber: " + newStudentNumber);
        try {
            HostelApplication hostelApplication = hostelApplicationService.getHostelApplicationByStudentNumber(newStudentNumber);
            if(hostelApplication == null) {                
                hostelAllocation.setStudentNumber(newStudentNumber.trim());
                eligibleStudent.setStudentNumber(newStudentNumber.trim());

                hostelEntityManager.merge(hostelAllocation);
                hostelEntityManager.merge(eligibleStudent);

                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, newStudentNumber + " has been updated",""));
                
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, newStudentNumber + " has already applied for hostel accommodation",""));
            }
        } catch(Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, newStudentNumber + " could not be updated",""));
            log.error("updateStudentNumber Error: " + e.getMessage(), e);
        }
    }
    
    public void updateStudentType() {
        log.info("newStudentType: " + newStudentType);
        try {

            HostelStudentType hostelStudentType = hostelApplicationService.getHostelStudentTypeByStudentType(newStudentType);
            eligibleStudent.setHostelStudentType(hostelStudentType);
            hostelEntityManager.merge(eligibleStudent);

            HostelApplication hostelApplication = hostelApplicationService.getHostelApplicationByStudentNumber(eligibleStudent.getStudentNumber());
            if(hostelApplication != null) {
                log.info("updateStudentType hostelApplicaton: " + hostelApplication.getApplicationNumber());
                hostelApplication.setStudentType(hostelStudentType);
                hostelApplication.setTotalAmount(hostelStudentType.getHostelFee());
                hostelEntityManager.merge(hostelApplication);
            }

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Student type update was successful",""));
            
        } catch(Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Student type update failed",""));
            log.error("updateStudentType Error: " + e.getMessage(), e);
        }
    }
    
    public String deleteStudent() {
        log.info("deleteStudent: " + eligibleStudent.getStudentNumber());
        try {

            hostelEntityManager.delete(HostelAllocation.class, hostelAllocation.getId());
            hostelEntityManager.delete(EligibleStudent.class, eligibleStudent.getId());

            HostelApplication hostelApplication = hostelApplicationService.getHostelApplicationByStudentNumber(newStudentNumber);
            if(hostelApplication != null) {
                hostelEntityManager.delete(HostelApplication.class, hostelApplication.getId());
            }

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, hostelStudentNumber + " has been deleted",""));
            
            return "eligiblestudentlist";
            
        } catch(Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, hostelStudentNumber + " could not be deleted",""));
            log.error("deleteStudent Error: " + e.getMessage(), e);
        }
        
        return "eligiblestudentupdate";
    }


    public void nextPage() {
       page++;
       findEligibleStudents();
    }
    
    public void previousPage() {
    	if(page > 0) {
    		page--;
    	}
        findEligibleStudents();
    }
        
    private void findEligibleStudents() {
              
        //String queryString = "select object(es) from EligibleStudent es where es.studentNumber like '" + searchString + "'";          
        String queryString = "select t from EligibleStudent t where lower(t.firstName) like '%" + searchString.toLowerCase() +
                    "%' or lower(t.studentNumber) like '" + searchString.toLowerCase() + "'";
            
        Query query = hostelEntityManager.getEntityManager().createQuery(queryString);
        //eligibleStudents = query.setMaxResults(pageSize).setFirstResult(page * pageSize).getResultList();
        eligibleStudents = query.getResultList();
        log.info("findEligibleStudents()");
    }
    
        
    public String selectEligibleStudent() {
        //this.eligibleStudent = eligibleStudent;
        log.info("selectEligibleStudent");
        hostelId = 0;
        hostelRoomId = 0;
        roomBedSpaceId = 0;
        newStudentNumber = "";

        hostelStudentNumber = eligibleStudent.getStudentNumber();
        log.info("hostelStudentNumber: " + hostelStudentNumber);
        log.info("session id: " + hostelSettingsService.getCurrentAcademicSession().getId());
        hostelAllocation = hostelApplicationService.getHostelAllocationByStudentNumberandAcademicSession(
                hostelSettingsService.getCurrentAcademicSession().getId(), eligibleStudent.getStudentNumber());

        log.info("hostelAllocation: " + hostelAllocation);
        
        return "eligiblestudentupdate";
    }
    
    public void changeHostelAllocation() {
        log.info("new hostel: " + hostelId + ", room: " + hostelRoomId + " , bed space: " + roomBedSpaceId);
        try {

            if(hostelId > 0 && hostelRoomId > 0 && roomBedSpaceId > 0) {
                hostel = hostelEntityManager.getEntityManager().find(Hostel.class, hostelId);
                hostelRoom = hostelEntityManager.getEntityManager().find(HostelRoom.class, hostelRoomId);
                roomBedSpace = hostelEntityManager.getEntityManager().find(HostelRoomBedSpace.class, roomBedSpaceId);
                if(hostelAllocation == null) {
                    hostelAllocation = new HostelAllocation();
                    hostelAllocation.setStudentNumber(hostelStudentNumber.trim());
                    hostelAllocation.setAcademicSession(hostelSettingsService.getCurrentAcademicSession());
                    hostelAllocation.setHostelRoom(hostelRoom);
                    hostelAllocation.setHostelRoomBedSpace(roomBedSpace);
                    hostelEntityManager.persist(hostelAllocation);

                } else {
                    hostelAllocation.setHostelRoom(hostelRoom);
                    hostelAllocation.setHostelRoomBedSpace(roomBedSpace);
                    hostelEntityManager.merge(hostelAllocation);
                }
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Hostel allocation update successful",""));
                
            } else {
                log.warn("hostel details not selected");
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select hostel, room and bedspace",""));
            }

        } catch(Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Hostel allocation update failed",""));
            log.error("changeHostelAllocation Error: " + e.getMessage(), e);
        }
    }
    
    public void addStudent() {
        
        try {
            
            eligibleStudent = hostelApplicationService.getEligibleStudentByStudentNumber(studentNumber.trim().toUpperCase());
            if(eligibleStudent != null) {
                log.warn("Eligible student already exists for " + studentNumber);
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Eligible student already exists for " + studentNumber,""));
                
                return;
            }

            if(hostelId > 0 && hostelRoomId > 0 && roomBedSpaceId > 0 && departmentId > 0) {
                eligibleStudent = new EligibleStudent();
                eligibleStudent.setStudentNumber(studentNumber.trim().toUpperCase());
                eligibleStudent.setFirstName(studentName.trim().toUpperCase());
                eligibleStudent.setDepartment(hostelEntityManager.getEntityManager().find(Department.class, departmentId).getName());
                HostelStudentType hostelStudentType = hostelApplicationService.getHostelStudentType(studentType);
                eligibleStudent.setHostelStudentType(hostelStudentType);
                eligibleStudent.setAcademicSession(hostelSettingsService.getCurrentAcademicSession().getSessionName());
                eligibleStudent.setDateUploaded(Calendar.getInstance().getTime());
                eligibleStudent.setUploadedBy(loginUserBean.getCurrentUser().getUserName());

                hostelEntityManager.persist(eligibleStudent);
                log.info("Eligible student created for " + studentNumber);
                
                hostel = hostelEntityManager.getEntityManager().find(Hostel.class, hostelId);
                hostelRoom = hostelEntityManager.getEntityManager().find(HostelRoom.class, hostelRoomId);
                roomBedSpace = hostelEntityManager.getEntityManager().find(HostelRoomBedSpace.class, roomBedSpaceId);
                
                hostelAllocation = new HostelAllocation();
                hostelAllocation.setStudentNumber(eligibleStudent.getStudentNumber());
                hostelAllocation.setAcademicSession(hostelSettingsService.getCurrentAcademicSession());
                hostelAllocation.setHostelRoom(hostelRoom);
                hostelAllocation.setHostelRoomBedSpace(roomBedSpace);
                hostelEntityManager.persist(hostelAllocation);
                log.info("Hostel allocation created for " + studentNumber);
                
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "New student was added successfully",""));
                
            } else {
                log.warn("hostel details not selected");
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Please select hostel, room and bedspace",""));
            }

        } catch(Exception e) {
            log.error("Error addStudent: " + e.getMessage());
        }
    }

    public void updateDepartmentList() {
        log.info("updateDepartmentList() " + facultyId);
        if (facultyId > 0) {            
            departments = hostelApplicationService.getDepartmentByFacultyId(facultyId);
            log.info("departments list = " + departments.size());
        }
    }
    
    public boolean isNextPageAvailable() {
        return eligibleStudents != null && eligibleStudents.size() == pageSize;
    }

    public boolean isPreviousPageAvailable() {
        if (page == 0) {
            return false;
        } else {
            return true;
        }
    }
    
    //@Factory(value = "studentSearchPattern", scope = ScopeType.EVENT)
    public String getSearchPattern() {
        return searchString == null
                ? "%" : '%' + searchString.toLowerCase().replace('*', '%') + '%';
    }
    
    public String close() {
        return "eligiblestudentlist";
    }

    
    public EligibleStudent getEligibleStudent() {
        return eligibleStudent;
    }

    public void setEligibleStudent(EligibleStudent eligibleStudent) {
        this.eligibleStudent = eligibleStudent;
    }

    public List<EligibleStudent> getEligibleStudents() {
        return eligibleStudents;
    }

    public void setEligibleStudents(List<EligibleStudent> eligibleStudents) {
        this.eligibleStudents = eligibleStudents;
    }

    public HostelAllocation getHostelAllocation() {
        return hostelAllocation;
    }

    public void setHostelAllocation(HostelAllocation hostelAllocation) {
        this.hostelAllocation = hostelAllocation;
    }
    
    public void updateRoomList() {
        if(hostelId > 0) {
            hostel = hostelEntityManager.getEntityManager().find(Hostel.class, hostelId);
            hostelRooms.clear();
            hostelRooms.addAll(hostelApplicationService.getHostelRoomByHostel(hostel));
        }          
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getNewStudentNumber() {
        return newStudentNumber;
    }

    public void setNewStudentNumber(String newStudentNumber) {
        this.newStudentNumber = newStudentNumber;
    }

    public String getNewStudentType() {
        return newStudentType;
    }

    public void setNewStudentType(String newStudentType) {
        this.newStudentType = newStudentType;
    }

    public Hostel getHostel() {
        if(hostelId > 0) {
            hostel = hostelEntityManager.getEntityManager().find(Hostel.class, hostelId);
        }
        return hostel;
    }

    public void setHostel(Hostel hostel) {
        this.hostel = hostel;
    }

    public HostelRoom getHostelRoom() {
        if(hostelRoomId > 0) {
            hostelRoom = hostelEntityManager.getEntityManager().find(HostelRoom.class, hostelRoomId);
        }
        return hostelRoom;
    }

    public void setHostelRoom(HostelRoom hostelRoom) {
        this.hostelRoom = hostelRoom;
    }

    public HostelRoomBedSpace getRoomBedSpace() {
        if(roomBedSpaceId > 0) {
            roomBedSpace = hostelEntityManager.getEntityManager().find(HostelRoomBedSpace.class, roomBedSpaceId);
        }
        return roomBedSpace;
    }

    public void setRoomBedSpace(HostelRoomBedSpace roomBedSpace) {
        this.roomBedSpace = roomBedSpace;
    }

    public List<HostelRoom> getHostelRooms() {
        return hostelRooms;
    }

    public void setHostelRooms(List<HostelRoom> hostelRooms) {
        this.hostelRooms = hostelRooms;
    }

    public String getHostelStudentNumber() {
        return hostelStudentNumber;
    }

    public void setHostelStudentNumber(String hostelStudentNumber) {
        this.hostelStudentNumber = hostelStudentNumber;
    }

    public HostelApplicationService getHostelApplicationService() {
        return hostelApplicationService;
    }

    public void setHostelApplicationService(HostelApplicationService hostelApplicationService) {
        this.hostelApplicationService = hostelApplicationService;
    }

    public HostelSettingsService getHostelSettingsService() {
        return hostelSettingsService;
    }

    public void setHostelSettingsService(HostelSettingsService hostelSettingsService) {
        this.hostelSettingsService = hostelSettingsService;
    }

    public HostelEntityManager getHostelEntityManager() {
        return hostelEntityManager;
    }

    public void setHostelEntityManager(HostelEntityManager hostelEntityManager) {
        this.hostelEntityManager = hostelEntityManager;
    }

    public int getHostelId() {
        return hostelId;
    }

    public void setHostelId(int hostelId) {
        this.hostelId = hostelId;
    }

    public long getHostelRoomId() {
        return hostelRoomId;
    }

    public void setHostelRoomId(long hostelRoomId) {
        this.hostelRoomId = hostelRoomId;
    }

    public int getRoomBedSpaceId() {
        return roomBedSpaceId;
    }

    public void setRoomBedSpaceId(int roomBedSpaceId) {
        this.roomBedSpaceId = roomBedSpaceId;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public LoginUserBean getLoginUserBean() {
        return loginUserBean;
    }

    public void setLoginUserBean(LoginUserBean loginUserBean) {
        this.loginUserBean = loginUserBean;
    }

    public String getStudentType() {
        return studentType;
    }

    public void setStudentType(String studentType) {
        this.studentType = studentType;
    }

    public long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(long departmentId) {
        this.departmentId = departmentId;
    }

    public long getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(long facultyId) {
        this.facultyId = facultyId;
    }
    
    public List<Department> getDepartments() {
        return departments;
    }

    public void setDepartments(List<Department> departments) {
        this.departments = departments;
    }

}
