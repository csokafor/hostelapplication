package com.perblo.hostel.bean;

import com.perblo.hostel.entity.HostelAllocation;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.faces.bean.ManagedProperty;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.perblo.hostel.entity.HostelApplication;
import com.perblo.hostel.entity.UnpaidHostelAllocation;
import com.perblo.hostel.entitymanager.HostelEntityManager;
import com.perblo.hostel.entitymanager.HostelEntityManagerImpl;
import com.perblo.hostel.service.HostelApplicationStatus;

import java.io.ByteArrayInputStream;
import javax.annotation.PreDestroy;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

import com.perblo.hostel.service.HostelSettingsService;
import org.apache.commons.codec.binary.Base64;
import org.primefaces.model.DefaultStreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.annotation.Secured;

@ManagedBean(name="hostelAppSearchBean")
@SessionScoped
@Secured("Hostel Application Admin")
public class HostelApplicationSearchBean implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(HostelApplicationSearchBean.class);

    @ManagedProperty(value = "#{hostelEntityManager}")
    HostelEntityManager hostelEntityManager;

    @ManagedProperty(value="#{hostelSettingsService}")
    private HostelSettingsService hostelSettingsService;

    private int pageSize = 20;    
    private int page;        
    private String searchString;    
    private Integer paymentStatus;    
    private Integer applicationStatus;    
    private Integer ballotStatus;    
    private boolean showUnpaidAllocation;
           
    private Date searchStartDate;       
    private Date searchEndDate;   

    private String schoolName;
    private HostelApplication hostelApplication;
    private String base64ApplicationNumber;
    private String applicationDate;
    private String hostelApplicationStatus;    
    private String hostelPaymentStatus;
    private String hostelBallotStatus;
    private boolean hasPaid;
    private DefaultStreamedContent passport;
    private List<HostelApplication> hostelApplications;
    
    public HostelApplicationSearchBean() {

    }
        
    public void search() {
       page = 0;
       queryHostelApplication();
    }

    public void nextPage() {
       page++;
       queryHostelApplication();
    }
    
    public void previousPage() {
    	if(page > 0) {
    		page--;
    	}
        queryHostelApplication();
     }
    
           
    private void queryHostelApplication() {
        String queryString = "";
        Query query = null;
        setShowUnpaidAllocation(false);
        
        if (paymentStatus != -1 && ballotStatus != -1) {
            queryString = "select t from HostelApplication t where ((lower(t.firstName) like '%" + searchString.toLowerCase()
                    + "%' or lower(t.lastName) like '%" + searchString.toLowerCase() + "%' or lower(t.studentNumber) like '%" + searchString.toLowerCase() + "%') "
                    + "and t.paymentStatus = ?1 and t.ballotStatus = ?2) and t.applicationDate between ?3 and ?4 order by t.applicationDate";
            query = hostelEntityManager.getEntityManager().createQuery(queryString);
            query.setParameter(1, paymentStatus);
            query.setParameter(2, ballotStatus);
            query.setParameter(3, searchStartDate==null?new Date():searchStartDate);
            query.setParameter(4, searchEndDate==null?new Date():searchEndDate);
            if(paymentStatus == HostelApplicationStatus.NOT_PAID && ballotStatus == HostelApplicationStatus.SUCCESSFUL) {
                log.info("unpaid successful allocations");
                setShowUnpaidAllocation(true);
            }
        } else if (paymentStatus == -1 && ballotStatus == -1) {
            queryString = "select t from HostelApplication t where (lower(t.firstName) like '%" + searchString.toLowerCase()
                    + "%' or lower(t.lastName) like '%" + searchString.toLowerCase() + "%' or lower(t.studentNumber) like '%" + searchString.toLowerCase() + "%') "
                    + " and t.applicationDate between ?1 and ?2 order by t.applicationDate";
            query = hostelEntityManager.getEntityManager().createQuery(queryString);
            query.setParameter(1, searchStartDate==null?new Date():searchStartDate);
            query.setParameter(2, searchEndDate==null?new Date():searchEndDate);
        } else if (paymentStatus == -1) {
            queryString = "select t from HostelApplication t where ((lower(t.firstName) like '%" + searchString.toLowerCase()
                    + "%' or lower(t.lastName) like '%" + searchString.toLowerCase() + "%' or lower(t.studentNumber) like '%" + searchString.toLowerCase() + "%') "
                    + " and t.ballotStatus = ?1) and t.applicationDate between ?2 and ?3 order by t.applicationDate";
            query = hostelEntityManager.getEntityManager().createQuery(queryString);
            query.setParameter(1, ballotStatus);
            query.setParameter(2, searchStartDate==null?new Date():searchStartDate);
            query.setParameter(3, searchEndDate==null?new Date():searchEndDate);
        } else if (ballotStatus == -1) {
            queryString = "select t from HostelApplication t where ((lower(t.firstName) like '%" + searchString.toLowerCase()
                    + "%' or lower(t.lastName) like '%" + searchString.toLowerCase() + "%' or lower(t.studentNumber) like '%" + searchString.toLowerCase() + "%') "
                    + " and t.paymentStatus = ?1) and t.applicationDate between ?2 and ?3 order by t.applicationDate";
            query = hostelEntityManager.getEntityManager().createQuery(queryString);
            query.setParameter(1, paymentStatus);
            query.setParameter(2, searchStartDate==null?new Date():searchStartDate);
            query.setParameter(3, searchEndDate==null?new Date():searchEndDate);
        }

        //hostelApplications = query.setMaxResults(pageSize).setFirstResult(page * pageSize).getResultList();
        hostelApplications = query.getResultList();
    }
    
    public void removeUnpaidHostelAllocations() {
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DAY_OF_YEAR, -7);
            int counter = 0;
            
            Query query = hostelEntityManager.getEntityManager().createNamedQuery("getUnpaidHostelApplicationByDate");
            query.setParameter(1, calendar.getTime());
            List<HostelApplication> unpaidApplications = (List<HostelApplication>)query.getResultList();
            
            for(HostelApplication hostelApp : unpaidApplications) {
                log.info("unpaid hostel application " + hostelApp.getId());
                UnpaidHostelAllocation unpaidHostelAllocation = new UnpaidHostelAllocation();
                HostelAllocation hostelAllocation = hostelApp.getHostelAllocation();
                
                if(hostelAllocation != null) {
                    unpaidHostelAllocation.setAcademicSession(hostelApp.getHostelAllocation().getAcademicSession());
                    unpaidHostelAllocation.setHostelRoom(hostelApp.getHostelAllocation().getHostelRoom());
                    unpaidHostelAllocation.setHostelRoomBedSpace(hostelApp.getHostelAllocation().getHostelRoomBedSpace());
                    unpaidHostelAllocation.setStudentNumber(hostelApp.getHostelAllocation().getStudentNumber());
                    hostelEntityManager.persist(unpaidHostelAllocation);

                    hostelApp.setHostelAllocation(null);
                    hostelApp.setBallotStatus(HostelApplicationStatus.PENDING);
                    hostelEntityManager.merge(hostelApp);
                    
                    hostelEntityManager.delete(HostelAllocation.class, hostelAllocation.getId());
                    log.info("removed allocation for " + hostelApp.getStudentNumber() + ", applicationdate = " + formatDate(hostelApp.getApplicationDate()));
                    counter++;
                }
            }
              
            FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, counter + " allocation removed.","")); 
                        
        } catch(Exception e) {
            log.error("Exception in removeUnpaidHostelAllocations: " + e.getMessage());
        }
    }
    
    public String getHostelApplication(HostelApplication hostelApp) {
        log.info("inside getHostelApplication: " + hostelApp.getStudentNumber());
        try {
            hostelApplication = hostelEntityManager.getEntityManager().find(HostelApplication.class, hostelApp.getId());
            setBase64ApplicationNumber(Base64.encodeBase64String(hostelApplication.getApplicationNumber().getBytes()));
        } catch(Exception e) {
            log.error("Exception in getHostelApplication: " + e.getMessage());
        }
        return "printhostelapplication";
    }

    public String getHostelRoom(HostelApplication hostelApp) {
        String room = "";
        try {            
            if(hostelApp.getHostelAllocation() == null) {
                log.info(hostelApp.getStudentNumber() + " student has not been allocated a hostel room.");
                room = "Not Allocated";
            } else {
                HostelAllocation hostelAllocation = hostelApp.getHostelAllocation();
                room = "Room " + hostelAllocation.getHostelRoom().getRoomNumber() + " " + hostelAllocation.getHostelRoom().getHostel().getHostelName();
            }

        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return room;
    }
    
    public DefaultStreamedContent getPassport() {
        if(hostelApplication.getPassportData() != null) {
            passport = new DefaultStreamedContent(
                    new ByteArrayInputStream(hostelApplication.getPassportData()), 
                    hostelApplication.getPassportContentType());
        }
        return passport;
    }

    public void setPassport(DefaultStreamedContent passport) {
        this.passport = passport;
    }

    public boolean isNextPageAvailable() {
        return hostelApplications != null && hostelApplications.size() == pageSize;
    }

    public boolean isPreviousPageAvailable() {
        if (page == 0) {
            return false;
        } else {
            return true;
        }
    }

    //@Factory(value = "hostelApplicationSearchPattern", scope = ScopeType.EVENT)
    public String getSearchPattern() {
        return searchString == null
                ? "%" : '%' + searchString.toLowerCase().replace('*', '%') + '%';
    }

    public String getApplicationStatus(int applicationStatus) {
        return HostelApplicationStatus.getApplicationStatus(applicationStatus);
    }
    
    public String getPaymentStatus(int paymentStatus) {
        return HostelApplicationStatus.getPaymentStatus(paymentStatus);
    }
    
    public String getBallotStatus(int ballotStatus) {
        return HostelApplicationStatus.getBallotStatus(ballotStatus);
    }

    public String formatDate(Date applicationDate) {
        String dateString = new java.text.SimpleDateFormat("dd-MMM-yyyy").format(applicationDate);
        return dateString;
    }

    public String formatDate(java.sql.Date applicationDate) {
        String dateString = new java.text.SimpleDateFormat("dd-MMM-yyyy").format(applicationDate);
        return dateString;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getSearchString() {
        return searchString;
    }

    public void setPaymentStatus(Integer paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Integer getPaymentStatus() {
        return paymentStatus;
    }

    public Integer getBallotStatus() {
        return ballotStatus;
    }

    public void setBallotStatus(Integer ballotStatus) {
        this.ballotStatus = ballotStatus;
    }

    public void setApplicationStatus(Integer applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public Integer getApplicationStatus() {
        return applicationStatus;
    }

    public boolean isShowUnpaidAllocation() {
        return showUnpaidAllocation;
    }

    public void setShowUnpaidAllocation(boolean showUnpaidAllocation) {
        this.showUnpaidAllocation = showUnpaidAllocation;
    }

    public Date getSearchEndDate() {
        return searchEndDate;
    }

    public void setSearchEndDate(Date searchEndDate) {
        this.searchEndDate = searchEndDate;
    }

    public Date getSearchStartDate() {
        return searchStartDate;
    }

    public void setSearchStartDate(Date searchStartDate) {
        this.searchStartDate = searchStartDate;
    }

    public HostelApplication getHostelApplication() {
        return hostelApplication;
    }

    public void setHostelApplication(HostelApplication hostelApplication) {
        this.hostelApplication = hostelApplication;
    }

    public String getHostelApplicationStatus() {
        return HostelApplicationStatus.getApplicationStatus(hostelApplication.getApplicationStatus());
    }

    public String getHostelPaymentStatus() {
        return HostelApplicationStatus.getPaymentStatus(hostelApplication.getPaymentStatus());
    }

    public String getHostelBallotStatus() {
        return HostelApplicationStatus.getBallotStatus(hostelApplication.getBallotStatus());
    }
    
    public String getApplicationDate() {
        String requestDateString = new java.text.SimpleDateFormat("dd-MMM-yyyy").format(hostelApplication.getApplicationDate());
        return requestDateString;
    }

    public void setApplicationDate(String applicationDate) {
        this.applicationDate = applicationDate;
    }
    
    public boolean isHasPaid() {
        if (hostelApplication == null) {
            return true;
        } else {
            if (hostelApplication.getPaymentStatus() == HostelApplicationStatus.PAID) {
                return true;
            } else {
                return false;
            }
        }
    }

    public List<HostelApplication> getHostelApplications() {
        return hostelApplications;
    }

    public void setHostelApplications(List<HostelApplication> hostelApplications) {
        this.hostelApplications = hostelApplications;
    }

    public String getBase64ApplicationNumber() {
        return base64ApplicationNumber;
    }

    public void setBase64ApplicationNumber(String base64ApplicationNumber) {
        this.base64ApplicationNumber = base64ApplicationNumber;
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

    public String getSchoolName() {
        schoolName = hostelSettingsService.getSchoolName();
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }
}