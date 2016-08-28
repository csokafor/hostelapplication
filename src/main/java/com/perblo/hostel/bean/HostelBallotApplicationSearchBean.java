package com.perblo.hostel.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import com.perblo.hostel.entity.HostelBallotApplication;
import com.perblo.hostel.helper.HostelApplicationStatus;
import com.perblo.hostel.listener.HostelEntityManagerListener;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.apache.log4j.Logger;
import org.springframework.security.access.annotation.Secured;

@ManagedBean(name="hostelBallotSearchBean")
@SessionScoped
@Secured("Hostel Application Admin")
public class HostelBallotApplicationSearchBean implements Serializable {

    private static final Logger log = Logger.getLogger(HostelBallotApplicationSearchBean.class);
    private EntityManager entityManager;  
	       
    private int pageSize = 20;    
    private int page;        
    private String searchString;    
    private Integer paymentStatus;    
    private Integer applicationStatus;    
    private Integer ballotStatus;    
    private boolean showUnpaidAllocation;       
    private Date searchStartDate;       
    private Date searchEndDate;    
        
    private HostelBallotApplication hostelBallotApplication;
    
    String applicationDate;
    private String hostelBallotApplicationStatus;    
    private String hostelPaymentStatus;
    private String hostelBallotStatus;
    private boolean hasPaid;
                
    private List<HostelBallotApplication> hostelBallotApplications;
    
    public HostelBallotApplicationSearchBean() {
        this.entityManager = HostelEntityManagerListener.createEntityManager();   
    }
    
    public void search() {
       page = 0;
       queryHostelBallotApplication();
    }

    public void nextPage() {
       page++;
       queryHostelBallotApplication();
    }
    
    public void previousPage() {
    	if(page > 0) {
    		page--;
    	}
        queryHostelBallotApplication();
     }
    
           
    private void queryHostelBallotApplication() {
        String queryString = "";
        Query query = null;
        setShowUnpaidAllocation(false);
        
        if (ballotStatus != -1) {
            queryString = "select t from HostelBallotApplication t where ((lower(t.firstName) like '%" + searchString + "%'"
                    + " or lower(t.lastName) like '%" + searchString + "%' or lower(t.studentNumber) like '" + searchString + "') "
                    + "and t.ballotStatus = ?1) and t.applicationDate between ?2 and ?3 order by t.applicationDate";
            query = entityManager.createQuery(queryString);            
            query.setParameter(1, ballotStatus);
            query.setParameter(2, searchStartDate==null?new Date():searchStartDate);
            query.setParameter(3, searchEndDate==null?new Date():searchEndDate);
            
        } else if (ballotStatus == -1) {
            queryString = "select t from HostelBallotApplication t where (lower(t.firstName) like '%" + searchString + "%'"
                    + " or lower(t.lastName) like '%" + searchString + "%' or lower(t.studentNumber) like '" + searchString + "') "
                    + " and t.applicationDate between ?1 and ?2 order by t.applicationDate";
            query = entityManager.createQuery(queryString);
            query.setParameter(1, searchStartDate==null?new Date():searchStartDate);
            query.setParameter(2, searchEndDate==null?new Date():searchEndDate);
        
        } 
        hostelBallotApplications = query.getResultList();
        //hostelBallotApplications = query.setMaxResults(pageSize).setFirstResult(page * pageSize).getResultList();
    }
    
        
    public String getHostelBallotApplication(HostelBallotApplication hostelApp) {
        log.info("inside getHostelBallotApplication: " + hostelApp.getStudentNumber());
        try {
            hostelBallotApplication = entityManager.find(HostelBallotApplication.class, hostelApp.getId());
            
        } catch(Exception e) {
            log.error("Exception in getHostelBallotApplication: " + e.getMessage());
        }
        
        return "printballotapplication";
    }

        

    public boolean isNextPageAvailable() {
        return hostelBallotApplications != null && hostelBallotApplications.size() == pageSize;
    }

    public boolean isPreviousPageAvailable() {
        if (page == 0) {
            return false;
        } else {
            return true;
        }
    }

    
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

    public HostelBallotApplication getHostelBallotApplication() {
        return hostelBallotApplication;
    }

    public void setHostelBallotApplication(HostelBallotApplication hostelBallotApplication) {
        this.hostelBallotApplication = hostelBallotApplication;
    }

    public String getHostelApplicationStatus() {
        return HostelApplicationStatus.getApplicationStatus(hostelBallotApplication.getApplicationStatus());
    }

   
    public String getHostelBallotStatus() {
        return HostelApplicationStatus.getBallotStatus(hostelBallotApplication.getBallotStatus());
    }
    
    public String getApplicationDate() {
        String requestDateString = new java.text.SimpleDateFormat("dd-MMM-yyyy").format(hostelBallotApplication.getApplicationDate());
        return requestDateString;
    }

    public void setApplicationDate(String applicationDate) {
        this.applicationDate = applicationDate;
    }

    public List<HostelBallotApplication> getHostelBallotApplications() {
        return hostelBallotApplications;
    }

    public void setHostelBallotApplications(List<HostelBallotApplication> hostelBallotApplications) {
        this.hostelBallotApplications = hostelBallotApplications;
    }
       
    @PreDestroy
    public void destroyBean() {
        if(entityManager.isOpen())
            entityManager.close();
        entityManager = null;        
    }
    
}