/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.perblo.security;

import com.perblo.hostel.entity.User;
import com.perblo.hostel.listener.HostelEntityManagerListener;
import java.util.List;
import javax.annotation.PreDestroy;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 *
 * @author cokafor
 */
@Component
public class UserDAO {
    private static final Logger log = Logger.getLogger(UserDAO.class);    
    private EntityManager entityManager;
    
    public UserDAO() {                
    }
    
    public EntityManager getEntityManager() {
        if(entityManager == null) {
            entityManager = HostelEntityManagerListener.createEntityManager();
        }
        log.info("UserDAO entityManager: " + this.entityManager.toString());
        return entityManager;
    }
    
    public User authenticateUser(String userName, String password) {
        User user = null;
        try {
            Query query = getEntityManager().createNamedQuery("getUserByUsername");
            query.setParameter(1, userName);
            List results = query.getResultList();
            log.info("getUserByUsername result: " + results.size());
            if (results.size() > 0) {
                user = (User)results.get(0);
                if(user.getUserPassword().equals(password)) {
                    log.info("correct user password");
                } else {
                    user = null;
                    log.warn("incorrect user password");
                }
            } else {
                log.warn("user not found");
            }
        } catch(Exception e) {
            log.error("authenticateUser: " + e.getMessage(), e);
        }
                    
        return user;
    }
    
    @PreDestroy
    public void destroyBean() {
        if(entityManager.isOpen())
            entityManager.close();
        entityManager = null;       
    }
}
