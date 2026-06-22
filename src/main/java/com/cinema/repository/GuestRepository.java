package com.cinema.repository;

import com.cinema.entity.Camp;
import com.cinema.entity.Guest;
import com.cinema.util.HibernateUtil;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class GuestRepository extends GenericRepository{
    public GuestRepository(){super(Guest.class);}

    /* Все гости с бронированиями за 1 запрос */
    public List<Guest> findAllWithBooking() {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery(
                    "SELECT DISTINCT g FROM Guest g LEFT JOIN FETCH g.bookings b LEFT JOIN FETCH b.housing h " +
                            "LEFT JOIN FETCH h.type " +
                            "ORDER BY g.id",
                    Guest.class).getResultList();
        }
    }
}
