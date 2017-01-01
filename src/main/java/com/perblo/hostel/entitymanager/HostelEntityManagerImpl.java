package com.perblo.hostel.entitymanager;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;

/**
 * Created by cokafor on 12/21/2016.
 */
@Repository(value = "hostelEntityManager")
@Transactional
public class HostelEntityManagerImpl implements HostelEntityManager {
    private EntityManager entityManager;

    public HostelEntityManagerImpl() {
    }

    @PersistenceContext(unitName = "hostelPU")
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EntityManager getEntityManager() {
        return this.entityManager;
    }

    public Object merge(Object object) throws Exception {
        return entityManager.merge(object);
    }

    public void persist(Object object) throws Exception {
        entityManager.persist(object);
    }

    public <T> T findById(Class<T> objectClass, Serializable id) throws Exception {
        return entityManager.find(objectClass, id);
    }

    public void delete(Class objectClass, Serializable id) throws Exception {
        entityManager.remove(entityManager.find(objectClass, id));
    }

}
