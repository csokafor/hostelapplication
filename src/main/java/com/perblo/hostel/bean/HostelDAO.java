/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.perblo.hostel.bean;

import java.io.Serializable;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import org.apache.log4j.Logger;

/**
 *
 * @author cokafor
 */
//@ManagedBean(name="hostelDAO")
//@SessionScoped
public class HostelDAO implements Serializable {
    private static final Logger log = Logger.getLogger(HostelDAO.class);
    private static EntityManagerFactory emf = Persistence.createEntityManagerFactory("hostelPU");
    private EntityManager entityManager;
    
    public HostelDAO() {        
    }
    
    public static EntityManagerFactory getEntityManagerFactory() {
        if(emf == null) {
            emf = Persistence.createEntityManagerFactory("hostelPU");
        }
        return emf;
    }

    public EntityManager getEntityManager() {
        if(entityManager == null) {
            entityManager = HostelDAO.getEntityManagerFactory().createEntityManager();
        }
        log.info("HostelDAO entityManager: " + this.entityManager.toString());
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    
}
