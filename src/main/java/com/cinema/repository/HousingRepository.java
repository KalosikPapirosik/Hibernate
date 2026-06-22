package com.cinema.repository;

import com.cinema.entity.Housing;
import com.cinema.util.HibernateUtil;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class HousingRepository extends GenericRepository{
    public HousingRepository() {super(Housing.class);}

    public Optional<Housing> findByIdWithTypeOfHousing(int id) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            List<Housing> result = em.createQuery(
                    "SELECT H FROM Housing H LEFT JOIN FETCH H.type WHERE H.id = :id",
                    Housing.class).setParameter("id", id).getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        }
    }

    public Optional<Housing> findByIdWithDetails(int id) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            List<Housing> result = em.createQuery(
                    "SELECT DISTINCT h FROM Housing h JOIN FETCH h.type JOIN FETCH h.condition JOIN FETCH h.camp " +
                            "WHERE h.id = :id",
                    Housing.class).setParameter("id", id).getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        }
    }

    public List<Housing> findByTypeId(int typeId){
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery(
                    "SELECT DISTINCT h FROM Housing h JOIN FETCH h.type JOIN FETCH h.condition JOIN FETCH h.camp " +
                            "WHERE h.type.id = :typeId " +
                            "ORDER BY h.id",
                    Housing.class).setParameter("typeId", typeId).getResultList();
        }
    }
}
