package com.cinema.repository;

import com.cinema.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.util.List;
import java.util.Optional;

/**
 * Generic Repository - CRUD операции (Create/Read/Update/Delete) для любой сущности через Hibernate.
 * Обычный репозиторий наследует и получает реализацию типовых операций, так как такой код шаблонный
 *
 * Почему нужен Repository?
 * Репозиторий скрывает от приложения работу с БД, говорят "искапсулирует". Это помогает поменять 
 * подход к хранению данных без переписывания кода
 * Сущности теперь не знают как их хранят и как их обрабатывают в БД, что делает их код чистым, без работы с БД
 * Данный код работы с БД пишется в репозиториях
 */
public class GenericRepository<T, ID> {

    private final Class<T> entityClass;

    public GenericRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    public List<T> findAll() {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery("FROM " + entityClass.getSimpleName(), entityClass)
                    .getResultList();
        }
    }

    public Optional<T> findById(ID id) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return Optional.ofNullable(em.find(entityClass, id));
        }
    }

    public T save(T entity) {
        EntityManager em = HibernateUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(entity);
            tx.commit();
            return entity;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public T update(T entity) {
        EntityManager em = HibernateUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T merged = em.merge(entity);
            tx.commit();
            return merged;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public boolean deleteById(ID id) {
        EntityManager em = HibernateUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            T entity = em.find(entityClass, id);
            if (entity != null) {
                em.remove(entity);
                tx.commit();
                return true;
            }
            tx.rollback();
            return false;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public long count() {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery("SELECT COUNT(e) FROM " + entityClass.getSimpleName() + " e", Long.class)
                    .getSingleResult();
        }
    }

    public boolean existsById(ID id) {
        return findById(id).isPresent();
    }

    protected Class<T> getEntityClass() {
        return entityClass;
    }
}
