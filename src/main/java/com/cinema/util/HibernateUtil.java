package com.cinema.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Управление жизненным циклом EntityManagerFactory.
 *
 * EntityManagerFactory создаётся ОДИН раз 
 * EntityManager создаётся ДЛЯ КАЖДОЙ операции
 */
public class HibernateUtil {

    private static final Logger log = LoggerFactory.getLogger(HibernateUtil.class);
    private static volatile EntityManagerFactory emf;

    private HibernateUtil() {}

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            synchronized (HibernateUtil.class) {
                if (emf == null) {
                    emf = Persistence.createEntityManagerFactory("cinema-pu");
                    log.info("EntityManagerFactory инициализирован (persistence-unit: cinema-pu)");
                }
            }
        }
        return emf;
    }

    /**
     * Создаёт новый EntityManager.
     * Аналог ConnectionManager.getConnection() из JDBC.
     */
    public static EntityManager createEntityManager() {
        return getEntityManagerFactory().createEntityManager();
    }

    /**
     * Закрывает EntityManagerFactory.
     * Вызывать при завершении приложения.
     */
    public static void close() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            log.info("EntityManagerFactory закрыт");
        }
    }
}
