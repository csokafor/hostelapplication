package com.perblo.hostel.bean;

import com.perblo.hostel.entity.Hostel;
import com.perblo.hostel.entity.HostelAllocation;
import com.perblo.hostel.entity.HostelApplication;
import com.perblo.hostel.entity.HostelRoom;
import java.io.Serializable;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.perblo.hostel.entitymanager.HostelEntityManagerImpl;
import com.perblo.hostel.service.HostelApplicationService;
import com.perblo.hostel.service.HostelApplicationStatus;
import com.perblo.hostel.service.HostelConfig;
import com.perblo.hostel.service.HostelSettingsService;

import java.util.ArrayList;
import java.util.Set;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import org.apache.log4j.Logger;

@ManagedBean(name="hostelAdminSearchBean")
@SessionScoped
//@Restrict("#{s:hasRole('Hostel Admin')}")
public class HostelAdminSearchBean implements Serializable {
    private static final Logger log = Logger.getLogger(HostelAdminSearchBean.class);

    @ManagedProperty(value = "#{hostelEntityManager}")
    HostelEntityManagerImpl hostelEntityManager;

    private EntityManager entityManager;    
    
    @ManagedProperty(value="#{hostelApplicationService}")
    private HostelApplicationService hostelApplicationService;
         
    @ManagedProperty(value="#{hostelSettingsService}")
    private HostelSettingsService hostelSettingsService;

    @ManagedProperty(value="#{hostelConfig}")
    private HostelConfig hostelConfig;
        
    private int pageSize = 20;
    private int page;
    
    private Hostel hostel;    
    private int hostelId;
    private List<Hostel> hostels;
     
    private List<HostelRoom> hostelRooms;        
    private List<HostelRoom> allHostelRooms;
            
    public HostelAdminSearchBean() {
        this.entityManager = hostelEntityManager.getEntityManager();
        hostelRooms = new ArrayList<HostelRoom>();
        allHostelRooms = new ArrayList<HostelRoom>();
    }
    
    public void search() {
        page = 0;
        getHostelRooms();
    }

    public void nextPage() {
        page++;
        getHostelRooms();
    }

    public void previousPage() {
        if (page > 0) {
            page--;
        }
        getHostelRooms();
    }

    public List<HostelRoom> getHostelRooms() {

        Query query = entityManager.createNamedQuery("getHostelRoomByHostel");
        query.setParameter(1, hostelId);
        //hostelRooms = query.setMaxResults(pageSize).setFirstResult(page * pageSize).getResultList();
        hostelRooms = query.getResultList();
        log.info("getHostelRooms()");
        
        return hostelRooms;
    }

    public String getHostelOccupants(HostelRoom hostelRoom) {
        StringBuilder roomOccupants = new StringBuilder();
        HostelApplication hostelApplication = null;
        String studentName = "";
        
        Set<HostelAllocation> hostelAllocations = hostelRoom.getHostelAllocations();        
        for(HostelAllocation hostelAllocation : hostelAllocations) {
                        
            hostelApplication = hostelApplicationService.getHostelApplicationByAcademicSessionAndStudentNumber(
                    hostelSettingsService.getCurrentAcademicSession().getId(), hostelAllocation.getStudentNumber());

            if(hostelApplication !=  null && hostelApplication.getPaymentStatus() == HostelApplicationStatus.PAID) {
                
                studentName = hostelApplication.getFirstName() + " " + hostelApplication.getLastName();
                roomOccupants.append(hostelAllocation.getStudentNumber());
                roomOccupants.append("  ");
                roomOccupants.append(studentName);
                roomOccupants.append("\n");
                
            } 
            
        }
        
        return roomOccupants.toString();
    }
    
    public void printHostelList(Integer hostelId) {
        try {
            log.info("printHostelList()");
            Hostel printHostel = entityManager.find(Hostel.class, hostelId);
            allHostelRooms = new ArrayList<HostelRoom>();
            //Set<HostelRoom> hostelRooms = printHostel.getHostelRooms();
            
            for(HostelRoom hostelRoom : printHostel.getHostelRooms()) {
                allHostelRooms.add(hostelRoom);
            }
            
            log.info("allHostelRooms.size(): " + allHostelRooms.size());
            
        } catch(Exception e) {
            log.error(e.getMessage());
        }
    }

    public boolean isNextPageAvailable() {
        return hostelRooms != null && hostelRooms.size() == pageSize;
    }

    public boolean isPreviousPageAvailable() {
        if (page == 0) {
            return false;
        } else {
            return true;
        }
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

    public int getHostelId() {
        return hostelId;
    }

    public void setHostelId(int hostelId) {
        this.hostelId = hostelId;
    }

    public List<Hostel> getHostels() {
        return hostels;
    }

    public void setHostels(List<Hostel> hostels) {
        this.hostels = hostels;
    }
    
    public void setHostelRooms(List<HostelRoom> hostelRooms) {
        this.hostelRooms = hostelRooms;
    }

    
    public Hostel getHostel() {
        if(hostelId > 0) {
            hostel = entityManager.find(Hostel.class, hostelId);
        }
        return hostel;
    }

    public void setHostel(Hostel hostel) {
        this.hostel = hostel;
    }

    public List<HostelRoom> getAllHostelRooms() {
        return allHostelRooms;
    }

    public void setAllHostelRooms(List<HostelRoom> allHostelRooms) {
        this.allHostelRooms = allHostelRooms;
    }
        
    @PreDestroy
    public void destroyBean() {
        if(entityManager.isOpen())
            entityManager.close();
        entityManager = null;        
    }
    
}
