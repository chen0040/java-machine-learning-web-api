package com.github.chen0040.ml.repositories;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by root on 11/11/15.
 */
public class CassandraFactory {
    private EntityManager em;
    private EntityManagerFactory emf;

    public CassandraFactory(){
        emf = Persistence.createEntityManagerFactory("cassandra_pu");
        em = emf.createEntityManager();
    }

    private void setTtl(EntityManager em, String tableName, int ttl) {
        if(ttl < 1) return;

        Map<String, Integer> ttlValues = new HashMap<String, Integer>(0);
        ttlValues.put(tableName, new Integer(ttl));
        em.setProperty("ttl.per.request", true);
        em.setProperty("ttl.values", ttlValues);
    }

    public <T> boolean updateCSObject(T object, int ttl) {
        boolean success = true;
        EntityManager em = emf.createEntityManager();
        final String className = object.getClass().getSimpleName();
        setTtl(em, className, ttl);
        try {
            em.getTransaction().begin();
            em.merge(object);
            em.getTransaction().commit();
        }
        catch(Exception ex) {
            em.getTransaction().rollback();
            success = false;
        }
        finally {
            em.close();
        }
        return success;
    }

    public <T> boolean deleteCSObject(Class<?> type, Object id) {
        boolean success = true;
        EntityManager em = emf.createEntityManager();
        final String className = type.getSimpleName();
        try {
            em.getTransaction().begin();
            em.remove( em.find(type, id) );
            em.getTransaction().commit();
        }
        catch(Exception ex) {
            em.getTransaction().rollback();
            success = false;
        }
        finally {
            em.close();
        }
        return success;
    }

    @SuppressWarnings("unchecked")
    public <T> T getCSObjectById(Class<?> type, Object id) {
        if( !em.isOpen() ) {
            em = emf.createEntityManager();
        }
        T result = (T) em.find(type, id);
        em.close();
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> queryList(Class<?> type, String cql, Map<String, Object> params, int limit) {
        //logger.info(LOGTITLE + "CQL >> " + cql + ". Params: " + params);
        EntityManager em = emf.createEntityManager();
        Query findQuery = em.createQuery(cql, type);
        formatQuery(findQuery, params);
        if(limit > 1) {
            findQuery.setMaxResults(limit);
        }
        List<T> resultList = findQuery.getResultList();
        //logger.debug(LOGTITLE + "Number of Records fetched: " + resultList.size());
        em.close();
        return resultList;
    }


    private void formatQuery(Query query, Map<String, Object> params) {
        if(params != null && !params.isEmpty()) {
            for(String key: params.keySet()) {
                Object val = params.get(key);
                query.setParameter(key, val);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T queryUnique(Class<?> type, String cql, Map<String, Object> params) {
        EntityManager em = emf.createEntityManager();
        Query findQuery = em.createQuery(cql, type);
        formatQuery(findQuery, params);
        findQuery.setMaxResults(1);
        List<T> resultList = findQuery.getResultList();
        //logger.debug(LOGTITLE + "Number of Records fetched: " + resultList.size() );
        em.close();
        return (resultList.size() == 0 ? null : resultList.get(0));
    }

    public <T> boolean addCSObject(T object, int ttl) {
        boolean success = true;
        EntityManager em = emf.createEntityManager();
        final String className = object.getClass().getSimpleName();
        setTtl(em, className, ttl);
        try {
            em.getTransaction().begin();
            em.persist(object);
            em.getTransaction().commit();
        }
        catch(Exception ex) {
            em.getTransaction().rollback();
            success = false;
        }
        finally {
            em.close();
        }
        return success;
    }
}
