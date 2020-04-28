package com.spark.bitrade.util;

import org.hibernate.Session;
import org.hibernate.jpa.HibernateEntityManager;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/***
 * jpa hibernate session操作类
 * @author yangch
 * @time 2018.07.13 13:58 
 */

@Component
public class JpaSessionUtil {
    /*@PersistenceContext
    private EntityManager entityManager;

    HibernateEntityManager hEntityManager =null; // (HibernateEntityManager)entityManager;
    Session session = null; // hEntityManager.getSession();

    *//**
     * 获取hibernate session对象
     * @return
     *//*
    public Session getSession() {
        if(hEntityManager==null) {
            hEntityManager = (HibernateEntityManager) entityManager;
        }
        if(session==null) {
            session = hEntityManager.getSession();
        }

        return session;
    }

    //某一个对象清除出缓存session
    public void evict(Object obj){
        getSession().evict(obj);
    }*/
}
