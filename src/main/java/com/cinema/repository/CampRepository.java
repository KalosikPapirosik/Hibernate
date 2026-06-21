package com.cinema.repository;

import com.cinema.entity.Camp;
import com.cinema.util.HibernateUtil;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class CampRepository extends GenericRepository<Camp, Integer> {
    public CampRepository() {super(Camp.class);}

    /* Все лагеря с домами за 1 запрос */
    public List<Camp> findAllWithHousing() {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery(
                    "SELECT DISTINCT C FROM Camp C LEFT JOIN FETCH C.housing ORDER BY C.id",
                    Camp.class).getResultList();
        }
    }

    /* Один лагерь с домами */
    public Optional<Camp> findByIdWithHousing(int id) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            List<Camp> result = em.createQuery(
                    "SELECT C FROM Camp C LEFT JOIN FETCH C.housing WHERE C.id = :id",
                    Camp.class).setParameter("id", id).getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        }
    }

    /* Все лагеря с Инвентарем за 1 запрос */
    public List<Camp> findAllWithInventory() {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery(
                    "SELECT DISTINCT C FROM Camp C LEFT JOIN FETCH C.inventory ORDER BY C.id",
                    Camp.class).getResultList();
        }
    }

    /* Один лагерь с инвентарем */
    public Optional<Camp> findByIdWithInventory(int id) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            List<Camp> result = em.createQuery(
                    "SELECT C FROM Camp C LEFT JOIN FETCH C.inventory WHERE C.id = :id",
                    Camp.class).setParameter("id", id).getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        }
    }
}
