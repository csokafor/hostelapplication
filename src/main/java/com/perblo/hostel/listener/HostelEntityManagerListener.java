/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.perblo.hostel.listener;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.Logger;

/**
 *
 * @author cokafor
 */
public class HostelEntityManagerListener implements ServletContextListener {
    private static final Logger log = Logger.getLogger(HostelEntityManagerListener.class);
    private static EntityManagerFactory emf;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        emf = Persistence.createEntityManagerFactory("hostelPU");
        log.info("contextInitialized: EntityManagerFactory" + emf.toString());
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        log.info("contextDestroyed: EntityManagerFactory" + emf.toString());
        emf.close();
    }

    public static EntityManager createEntityManager() {
        if (emf == null) {
            throw new IllegalStateException("Context is not initialized yet.");
        }

        return emf.createEntityManager();
    }

    
}
