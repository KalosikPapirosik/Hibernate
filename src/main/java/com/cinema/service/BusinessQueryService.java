package com.cinema.service;

import com.cinema.util.HibernateUtil;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Примеры бизнес-запросов на JPQL для системы бронирования кемпингов.
 */
public class BusinessQueryService {

    // 1. Выручка по типам жилья
    public void revenueByHousingType() {
        printHeader("Выручка по типам жилья");
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            List<Object[]> results = em.createQuery("""
                    SELECT h.type.name,
                           COUNT(b),
                           SUM(h.cost)
                    FROM Booking b
                    JOIN b.housing h
                    GROUP BY h.type.name
                    ORDER BY SUM(h.cost) DESC
                    """, Object[].class).getResultList();

            System.out.printf("     %-22s %-12s %-15s%n", "Тип жилья", "Бронирований", "Выручка (₽)");
            System.out.println("     " + "─".repeat(51));
            for (Object[] row : results) {
                System.out.printf("     %-22s %-12d %-15.2f%n",
                        row[0], (long) row[1], (BigDecimal) row[2]);
            }
        }
        printDivider();
    }

    // 2. Заполняемость кемпов (активные брони / вместимость)
    public void campOccupancy() {
        printHeader("Заполняемость кемпов");
        OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            List<Object[]> results = em.createQuery("""
                    SELECT c.location,
                           SUM(h.capacity),
                           COUNT(DISTINCT b),
                           ROUND(CAST(COUNT(DISTINCT b) AS double) / NULLIF(COUNT(DISTINCT h), 0) * 100, 1)
                    FROM Camp c
                    JOIN c.housing h
                    LEFT JOIN h.booking b 
                    GROUP BY c.location
                    ORDER BY COUNT(DISTINCT b) DESC
                    """, Object[].class).getResultList();

            System.out.printf("     %-25s %-12s %-10s%n", "Кемп", "Актив. броней", "Загруз.%");
            System.out.println("     " + "─".repeat(61));
            for (Object[] row : results) {
                System.out.printf("     %-25s %-12d %-10.1f%n",
                        row[0], (long) row[2], (double) row[3]);
            }
        }
        printDivider();
    }

    // 3. Топ-5 гостей по сумме потраченных средств
    public void topGuestsBySpending() {
        printHeader("Топ-5 гостей по тратам");
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            List<Object[]> results = em.createQuery("""
                    SELECT b.guest.lastname || ' ' || b.guest.firstname,
                           COUNT(b),
                           SUM(b.housing.cost + COALESCE(b.additionalService.cost, 0))
                    FROM Booking b
                    GROUP BY b.guest.id, b.guest.lastname, b.guest.firstname
                    ORDER BY SUM(b.housing.cost + COALESCE(b.additionalService.cost, 0)) DESC
                    """, Object[].class)
                    .setMaxResults(5)
                    .getResultList();

            System.out.printf("     %-25s %-10s %-15s%n", "Гость", "Броней", "Потрачено (₽)");
            System.out.println("     " + "─".repeat(52));
            for (Object[] row : results) {
                System.out.printf("     %-25s %-10d %-15.2f%n",
                        row[0], (long) row[1], (BigDecimal) row[2]);
            }
        }
        printDivider();
    }

    // 4. Популярность дополнительных услуг
    public void additionalServicesPopularity() {
        printHeader("Популярность доп. услуг");
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            List<Object[]> results = em.createQuery("""
                    SELECT b.additionalService.label,
                           COUNT(b),
                           SUM(b.additionalService.cost),
                           AVG(b.additionalService.cost)
                    FROM Booking b
                    WHERE b.additionalService IS NOT NULL
                    GROUP BY b.additionalService.id, b.additionalService.label, b.additionalService.cost
                    ORDER BY COUNT(b) DESC
                    """, Object[].class).getResultList();

            System.out.printf("     %-25s %-10s %-15s %-10s%n", "Услуга", "Заказов", "Доход (₽)", "Сред.цена");
            System.out.println("     " + "─".repeat(62));
            for (Object[] row : results) {
                System.out.printf("     %-25s %-10d %-15.2f %-10.2f%n",
                        row[0], (long) row[1], row[2], row[3]);
            }
        }
        printDivider();
    }

    // 5. Динамика бронирований по датам (последние 30 дней)
    public void bookingTrendsLastMonth() {
        printHeader("Динамика бронирований (30 дней)");
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
            List<Object[]> results = em.createQuery("""
                    SELECT FUNCTION('DATE', b.dateOfStart),
                           COUNT(b),
                           SUM(b.housing.cost + COALESCE(b.additionalService.cost, 0)),
                           AVG(b.housing.cost + COALESCE(b.additionalService.cost, 0))
                    FROM Booking b
                    WHERE b.dateOfStart >= :thirtyDaysAgo
                    GROUP BY FUNCTION('DATE', b.dateOfStart)
                    ORDER BY FUNCTION('DATE', b.dateOfStart) DESC
                    """, Object[].class).setParameter("thirtyDaysAgo", thirtyDaysAgo).getResultList();

            System.out.printf("     %-12s %-10s %-15s %-12s%n", "Дата", "Броней", "Выручка (₽)", "Сред.чек");
            System.out.println("     " + "─".repeat(51));
            for (Object[] row : results) {
                System.out.printf("     %-12s %-10d %-15.2f %-12.2f%n",
                        row[0], (long) row[1], (BigDecimal) row[2], (Double) row[3]);
            }
        }
        printDivider();
    }


    // 6. Кросс продажи по типам жилья и услугам
    public void crossSalesAdditionalServicesWithHousings() {
        printHeader("Кросс продажи услуг");
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
            List<Object[]> results = em.createQuery("""
                    SELECT h.type.name,
                           s.label,
                           count(s),
                           SUM(s.cost)
                    FROM Booking b
                    JOIN b.housing h
                    JOIN b.additionalService s
                    GROUP BY h.type.name, s.label, s.id
                    ORDER BY COUNT(b) DESC
                    """, Object[].class).getResultList();

            System.out.printf("     %-12s %-10s %-15s %-12s%n","Тип жилья", "Название услуги", "Количество услуг","Выручка (₽)");
            System.out.println("     " + "─".repeat(51));
            for (Object[] row : results) {
                System.out.printf("     %-12s %-24s %d %-12.2f%n",
                        row[0], row[1], (long) row[2], (BigDecimal) row[3]);
            }
        }
        printDivider();
    }

    // 7. Гости, часто возвращающиеся
    public void guestTotheBack() {
        printHeader("Динамика возврата гостей");
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            OffsetDateTime thirtyDaysAgo = OffsetDateTime.now().minusDays(30);
            List<Object[]> results = em.createQuery("""
                    SELECT g.lastname || " " || g.firstname,
                           count(b),
                           SUM(b.housing.cost)
                    FROM Booking b
                    JOIN b.housing h
                    JOIN b.guest g
                    GROUP BY g.id, g.lastname, g.firstname
                    HAVING count(b) > 1
                    ORDER BY COUNT(b) DESC
                    """, Object[].class).getResultList();

            System.out.printf("     %-12s %-15s %-12s%n","Имя", "Количество броней","Выручка (₽)");
            System.out.println("     " + "─".repeat(51));
            for (Object[] row : results) {
                System.out.printf("     %-12s %d %-12.2f%n",
                        row[0], (long) row[1], (BigDecimal) row[2]);
            }
        }
        printDivider();
    }

    public void runAll() {
        revenueByHousingType();
        campOccupancy();
        topGuestsBySpending();
        additionalServicesPopularity();
        bookingTrendsLastMonth();
        crossSalesAdditionalServicesWithHousings();
        guestTotheBack();
    }

    private void printHeader(String title) {
        System.out.println();
        System.out.println("╔" + "═".repeat(title.length() + 4) + "╗");
        System.out.println("║  " + title + "  ║");
        System.out.println("╚" + "═".repeat(title.length() + 4) + "╝");
    }

    private void printDivider() {
        System.out.println("─".repeat(80));
    }
}
