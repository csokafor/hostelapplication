package com.perblo.hostel.entitymanager;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * Created by cokafor on 12/21/2016.
 */
public interface HostelEntityManager {

    public EntityManager getEntityManager();

    public Object merge(Object object) throws Exception;
    public void persist(Object object) throws Exception;
    public <T> T findById(Class <T> objectClass, Serializable id) throws Exception;
    public void delete(Class objectClass, Serializable id) throws Exception;
    /*
    public void deleteObject(Object object) throws Exception;
    public void deleteObject(Class objectClass, Serializable id) throws Exception;
    public List runDynamicNamedQuery(String queryName, HashMap<String,Object> parameterMap)throws Exception;
    public List runDynamicNativeQuery(String queryString,HashMap<Integer,Object> parameterMap) throws Exception;
    public List runDynamicNamedQuery(String queryName, HashMap<String,Object> parameterMap, int startFrom, int maxResult)throws Exception;
    public List runDynamicNativeQuery(String queryString,HashMap<Integer,Object> parameterMap, int startFrom, int maxResult) throws Exception;
    */
}
