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
                    "SELECT DISTINCT G FROM Guest G LEFT JOIN FETCH G.bookings ORDER BY G.id",
                    Guest.class).getResultList();
        }
    }
}
