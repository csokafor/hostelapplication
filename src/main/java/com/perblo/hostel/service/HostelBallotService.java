package com.perblo.hostel.service;

import com.perblo.hostel.entity.*;
import com.perblo.hostel.entitymanager.HostelEntityManager;
import com.perblo.hostel.entitymanager.HostelEntityManagerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedProperty;
import javax.faces.context.FacesContext;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.util.*;


/**
 * Created by cokafor on 12/24/2016.
 */
@Service(value="hostelBallotService")
@Transactional
public class HostelBallotService {

    private static final Logger log = LoggerFactory.getLogger(HostelBallotService.class);

    @Autowired
    private HostelEntityManager hostelEntityManager;

    @Autowired
    private HostelSettingsService hostelSettingsService;

    private static Map<Long,HostelBallotQuota> hostelBallotQuotaMap = null;
    private final Object hostelBallotLock = new Object();
    private Random random;


    public Map<Long, HostelBallotQuota> getHostelBallotQuotaMap() {
        if(hostelBallotQuotaMap == null) {
            hostelBallotQuotaMap = new HashMap<Long, HostelBallotQuota>();
            Query query = hostelEntityManager.getEntityManager().createNamedQuery("getAllHostelBallotQuota");
            List<HostelBallotQuota> hostelQuotas = (List<HostelBallotQuota>) query.getResultList();

            for(HostelBallotQuota hostelQuota : hostelQuotas) {
                hostelBallotQuotaMap.put(hostelQuota.getDepartment().getId(), hostelQuota);
            }
        }
        return hostelBallotQuotaMap;
    }

    private void setHostelBallotQuotaMap(Map<Long, HostelBallotQuota> hostelBallotQuotaMap) {
        HostelBallotService.hostelBallotQuotaMap = hostelBallotQuotaMap;
    }

    private void updateHostelBallotQuotaMap(HostelBallotQuota hostelBallotQuota) {
        hostelBallotQuotaMap.put(hostelBallotQuota.getDepartment().getId(), hostelBallotQuota);
    }

    public boolean doHostelBallot(EligibleBallotStudent eligibleBallotStudent) {
        boolean ballotStatus = false;
        try {
            Department department = eligibleBallotStudent.getDepartment();
            HostelBallotQuota hostelBallotQuota = getHostelBallotQuotaMap().get(department.getId());

            synchronized(hostelBallotLock) {
                if (eligibleBallotStudent.getGender().equalsIgnoreCase("Male") || eligibleBallotStudent.getGender().equalsIgnoreCase("M")) {
                    if (hostelBallotQuota.getUsedMaleQuota() < hostelBallotQuota.getNumberOfMaleStudents()) {
                        int randNo = random.nextInt(99);
                        log.info("hostelballot random no " + randNo);
                        if (randNo > 45) {
                            ballotStatus = true;
                            log.info(eligibleBallotStudent.getStudentNumber() + " ballot succesful" + randNo);
                            hostelBallotQuota.setUsedMaleQuota(hostelBallotQuota.getUsedMaleQuota() + 1);
                            hostelEntityManager.merge(hostelBallotQuota);
                            updateHostelBallotQuotaMap(hostelBallotQuota);
                        } else {
                            log.info(eligibleBallotStudent.getStudentNumber() + " ballot not succesful." + randNo);
                        }
                    } else {
                        log.info(department.getName() + " male quota filled");
                    }
                } else if(eligibleBallotStudent.getGender().equalsIgnoreCase("Female") || eligibleBallotStudent.getGender().equalsIgnoreCase("F")){
                    if (hostelBallotQuota.getUsedFemaleQuota() < hostelBallotQuota.getNumberOfFemaleStudents()) {
                        int randNo = random.nextInt(6);
                        if (randNo > 2) {
                            ballotStatus = true;
                            log.info(eligibleBallotStudent.getStudentNumber() + " ballot succesful"+ randNo);
                            hostelBallotQuota.setUsedFemaleQuota(hostelBallotQuota.getUsedFemaleQuota() + 1);
                            hostelEntityManager.merge(hostelBallotQuota);
                            updateHostelBallotQuotaMap(hostelBallotQuota);
                        } else {
                            log.info(eligibleBallotStudent.getStudentNumber() + " ballot not succesful." + randNo);
                        }
                    } else {
                        log.info(department.getName() + " female quota filled");
                        ballotStatus = false;
                    }
                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ballot for accommodation failed!",""));
                    log.error("Unknown gender for " + eligibleBallotStudent.getStudentNumber());
                }

            }

        } catch(Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ballot for accommodation failed!", ""));
            log.error(e.getMessage());
        }

        return ballotStatus;
    }

    public EligibleBallotStudent addEligibleBallotStudent(String studentNumber, String studentName, Department department,
                                                          String gender, String username) {
        EligibleBallotStudent eligibleBallotStudent = new EligibleBallotStudent();
        try {
            eligibleBallotStudent.setStudentNumber(studentNumber);
            eligibleBallotStudent.setFirstName(studentName);
            eligibleBallotStudent.setGender(gender);
            eligibleBallotStudent.setDepartment(department);

            eligibleBallotStudent.setAcademicSession(hostelSettingsService.getCurrentAcademicSession().getSessionName());
            eligibleBallotStudent.setDateUploaded(Calendar.getInstance().getTime());
            eligibleBallotStudent.setUploadedBy(username);

            hostelEntityManager.getEntityManager().persist(eligibleBallotStudent);
            log.info("Eligible student created for " + studentNumber);


        } catch(Exception e) {
            log.error("Error addEligibleBallotStudent: " + e.getMessage(), e);
        }

        return eligibleBallotStudent;
    }


    public HostelBallotApplication getHostelBallotApplicationByAcademicSessionAndStudentNumber(Integer sessionId, String studentNumber) {
        HostelBallotApplication hostelBallotApplication = null;
        Query query = hostelEntityManager.getEntityManager().createNamedQuery("getHostelBallotApplicationByAcademicSessionAndStudentNumber");
        query.setParameter(1, sessionId);
        query.setParameter(2, studentNumber);
        List results = query.getResultList();
        if (results.size() > 0) {
            hostelBallotApplication = (HostelBallotApplication) results.get(0);
        }
        return hostelBallotApplication;
    }

    public EligibleBallotStudent getEligibleBallotStudentByStudentNumber(String studentNumber) {
        EligibleBallotStudent eligibleBallotStudent = null;
        Query query = hostelEntityManager.getEntityManager().createNamedQuery("getEligibleBallotStudentByStudentNumber");
        query.setParameter(1, studentNumber);
        List results = query.getResultList();
        if (results.size() > 0) {
            eligibleBallotStudent = (EligibleBallotStudent) results.get(0);
        }
        return eligibleBallotStudent;
    }

    public HostelBallotApplication getHostelBallotApplicationByStudentNumberAndApplicationNumber(String studentNumber, String applicationNumber) {
        HostelBallotApplication hostelApp = null;
        try {
            Query queryObj = hostelEntityManager.getEntityManager().createNamedQuery(
                    "getHostelBallotApplicationByStudentNumberAndApplicationNumber");
            queryObj.setParameter(1, studentNumber);
            queryObj.setParameter(2, applicationNumber);
            List<HostelBallotApplication> results = queryObj.getResultList();

            if (results.isEmpty()) {
                hostelApp = null;
            } else {
                hostelApp = (HostelBallotApplication) results.get(0);
            }

        } catch(Exception e) {
            log.error("Exception in getHostelBallotApplicationByStudentNumberAndApplicationNumber: " + e.getMessage());
        }

        return hostelApp;
    }

    public HostelBallotApplication getHostelBallotApplicationByStudentNumber(String studentNumber) {
        HostelBallotApplication hostelApp = null;
        try {
            Query queryObj = hostelEntityManager.getEntityManager().createNamedQuery("getHostelBallotApplicationByStudentNumber");
            queryObj.setParameter(1, studentNumber);
            List<HostelBallotApplication> results = queryObj.getResultList();

            if (results.isEmpty()) {
                hostelApp = null;
            } else {
                hostelApp = (HostelBallotApplication) results.get(0);
            }

        } catch(Exception e) {
            log.error("Exception in getHostelBallotApplicationByStudentNumberAndApplicationNumber: " + e.getMessage());
        }

        return hostelApp;
    }

    public HostelApplication getHostelApplicationByPaymentTransactionId(long paymentTransactionId) {
        HostelApplication hostelApp = null;
        try {
            Query queryObj = hostelEntityManager.getEntityManager().createNamedQuery("getHostelApplicationByPaymentTransactionId");
            queryObj.setParameter(1, paymentTransactionId);
            List<HostelApplication> results = queryObj.getResultList();

            if (results.isEmpty()) {
                hostelApp = null;
            } else {
                hostelApp = (HostelApplication) results.get(0);
            }

        } catch(Exception e) {
            log.error("Exception in getHostelApplicationByPaymentTransactionId: " + e.getMessage());
        }

        return hostelApp;
    }

    public HostelEntityManager getHostelEntityManager() {
        return hostelEntityManager;
    }

    public void setHostelEntityManager(HostelEntityManager hostelEntityManager) {
        this.hostelEntityManager = hostelEntityManager;
    }
}
