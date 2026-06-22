package com.cinema.repository;

import com.cinema.entity.AdditionalServices;
import com.cinema.entity.Booking;
import com.cinema.entity.Guest;
import com.cinema.entity.Housing;
import com.cinema.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

public class BookingRepository extends GenericRepository {
    public BookingRepository() {super(Booking.class);}

    public List<Booking> findByGuestIdWithDetails(int guestId) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery(
                    "SELECT DISTINCT b FROM Booking b JOIN FETCH b.guest JOIN FETCH b.housing h JOIN FETCH h.type " +
                            "JOIN FETCH h.camp JOIN FETCH h.condition LEFT JOIN FETCH b.additionalService " +
                            "WHERE b.guest.id = :guestId " +
                            "ORDER BY b.dateOfStart",
                    Booking.class).setParameter("guestId", guestId).getResultList();
        }
    }

    public List<Booking> findByDateTimeBetween(OffsetDateTime from, OffsetDateTime to) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            return em.createQuery(
                    "FROM Booking b WHERE b.dateOfStart BETWEEN :from AND :to ORDER BY b.dateOfStart",
                    Booking.class).setParameter("from", from).setParameter("to", to).getResultList();
        }
    }

    public Optional<Booking> findByIdWithDetails(int id) {
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            List<Booking> result = em.createQuery(
                    "SELECT DISTINCT b FROM Booking b JOIN FETCH b.housing h JOIN FETCH h.type JOIN FETCH h.camp JOIN FETCH h.condition " +
                            "LEFT JOIN FETCH b.additionalService JOIN FETCH b.guest WHERE b.id = :id",
                    Booking.class).setParameter("id", id).getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        }
    }

    public int createBooking(int guestId, int housingId, Integer additionalServiceId,
                             OffsetDateTime dateOfStart, OffsetDateTime dateOfEnd,
                             OffsetDateTime dateOfService) {

        EntityManager em = HibernateUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Guest guest = em.find(Guest.class, guestId);
            Housing housing = em.find(Housing.class, housingId);
            AdditionalServices service = additionalServiceId != null
                    ? em.find(AdditionalServices.class, additionalServiceId)
                    : null;

            if (guest == null || housing == null || (additionalServiceId != null && service == null)) {
                throw new IllegalArgumentException("Guest, housing or additional service not found");
            }

            // Блокируем жилье на запись, чтобы два клиента не забронировали его одновременно
            em.lock(housing, LockModeType.PESSIMISTIC_WRITE);

            // Проверяем пересечение дат: существующая бронь пересекается с новой, если
            // её начало раньше конца новой И её конец позже начала новой
            Long overlapping = em.createQuery(
                            "SELECT COUNT(b) FROM Booking b WHERE b.housing.id = :housingId " +
                                    "AND b.dateOfStart < :end AND b.dateOfEnd > :start",
                            Long.class)
                    .setParameter("housingId", housingId)
                    .setParameter("start", dateOfStart)
                    .setParameter("end", dateOfEnd)
                    .getSingleResult();

            if (overlapping > 0) {
                tx.rollback();
                throw new IllegalStateException(
                        String.format("Housing #%d is already booked for the period %s to %s",
                                housingId, dateOfStart, dateOfEnd));
            }

            Booking booking = new Booking(guest, housing, service, dateOfStart, dateOfEnd, dateOfService);
            em.persist(booking);
            tx.commit();

            return booking.getId();

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e; // Пробрасываем дальше, чтобы сервисный слой мог обработать
        } finally {
            em.close();
        }
    }

}
