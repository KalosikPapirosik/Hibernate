package com.cinema.service.щдв;

import com.cinema.util.HibernateUtil;
import jakarta.persistence.EntityManager;

import java.math.BigDecimal;
import java.util.List;

/**
 * Примеры бизнес-запросов на JPQL.
 * 
 */
public class BusinessQueryService {

    // 1. Выручка по фильмам
    public void revenueByFilm() {
        printHeader("Выручка по фильмам");
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            // JPQL работает с сущностями, а не таблицами
            // t.screening.film.title - навигация по связям (вместо ручного JOIN)
            List<Object[]> results = em.createQuery("""
                    SELECT t.screening.film.title,
                           COUNT(t),
                           SUM(t.screening.basePrice * t.seat.priceCoefficient)
                    FROM Ticket t
                    GROUP BY t.screening.film.title
                    ORDER BY SUM(t.screening.basePrice * t.seat.priceCoefficient) DESC
                    """, Object[].class).getResultList();

            System.out.printf("     %-22s %-10s %-15s%n", "Фильм", "Билетов", "Выручка (₽)");
            System.out.println("     " + "─".repeat(49));
            for (Object[] row : results) {
                System.out.printf("     %-22s %-10d %-15.2f%n", row[0], (long) row[1], (BigDecimal) row[2]);
            }
        }
        printDivider();
    }

    // 2. Заполняемость залов
    public void hallOccupancy() {
        printHeader("Заполняемость залов");
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            List<Object[]> results = em.createQuery("""
                    SELECT s.hall.name,
                           s.hall.capacity,
                           COUNT(t),
                           ROUND(CAST(COUNT(t) AS double) / s.hall.capacity * 100, 1)
                    FROM Screening s
                    LEFT JOIN Ticket t ON t.screening = s
                    GROUP BY s.hall.name, s.hall.capacity
                    ORDER BY COUNT(t) DESC
                    """, Object[].class).getResultList();

            System.out.printf("     %-16s %-13s %-10s %-10s%n", "Зал", "Вместимость", "Билетов", "Заполн.%");
            System.out.println("     " + "─".repeat(51));
            for (Object[] row : results) {
                System.out.printf("     %-16s %-13d %-10d %-10.1f%n", row[0], (short) row[1], (long) row[2], (double) row[3]);
            }
        }
        printDivider();
    }

    // 3. Топ-3 посетителя по количеству билетов
    public void topVisitors() {
        printHeader("Топ-3 посетителя");
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            List<Object[]> results = em.createQuery("""
                    SELECT t.visitor.name, COUNT(t)
                    FROM Ticket t
                    GROUP BY t.visitor.name
                    ORDER BY COUNT(t) DESC
                    """, Object[].class)
                    .setMaxResults(3) // ← Hibernate добавит LIMIT 3
                    .getResultList();

            System.out.printf("     %-25s %-10s%n", "Посетитель", "Билетов");
            System.out.println("     " + "─".repeat(37));
            for (Object[] row : results) {
                System.out.printf("     %-25s %-10d%n", row[0], (long) row[1]);
            }
        }
        printDivider();
    }

    // 4. Фильмы с количеством жанров
    public void filmsWithGenreCount() {
        printHeader("Фильмы и количество жанров");
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            // JPQL навигация по M:N связи: f.genres — коллекция жанров
            List<Object[]> results = em.createQuery("""
                    SELECT f.title, f.duration, SIZE(f.genres)
                    FROM Film f
                    ORDER BY SIZE(f.genres) DESC
                    """, Object[].class).getResultList();

            System.out.printf("     %-22s %-14s %-10s%n", "Фильм", "Длительность", "Жанров");
            System.out.println("     " + "─".repeat(48));
            for (Object[] row : results) {
                System.out.printf("     %-22s %-14s %-10d%n", row[0], row[1], (int) row[2]);
            }
        }
        printDivider();
    }

    // 5. Сеансы с фильмом и залом (демо JOIN FETCH через DTO-проекцию)
    public void screeningsWithDetails() {
        printHeader("Сеансы с фильмом и залом");
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            List<Object[]> results = em.createQuery("""
                    SELECT s.film.title, s.hall.name, s.dateTime, s.basePrice
                    FROM Screening s
                    ORDER BY s.dateTime
                    """, Object[].class).getResultList();

            System.out.printf("     %-22s %-12s %-25s %-10s%n", "Фильм", "Зал", "Дата/время", "Цена");
            System.out.println("     " + "─".repeat(71));
            for (Object[] row : results) {
                System.out.printf("     %-22s %-12s %-25s %-10s%n", row[0], row[1], row[2], row[3]);
            }
        }
        printDivider();
    }

    // 6. Жанры по популярности (по количеству билетов)
    public void genrePopularity() {
        printHeader("Жанры по популярности");
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            // Навигация через M:N: t.screening.film.genres - 3 уровня вложенности!           
            List<Object[]> results = em.createQuery("""
                    SELECT g.name, COUNT(t)
                    FROM Ticket t
                    JOIN t.screening.film.genres g
                    GROUP BY g.name
                    ORDER BY COUNT(t) DESC
                    """, Object[].class).getResultList();

            System.out.printf("     %-20s %-10s%n", "Жанр", "Билетов");
            System.out.println("     " + "─".repeat(32));
            for (Object[] row : results) {
                System.out.printf("     %-20s %-10d%n", row[0], (long) row[1]);
            }
        }
        printDivider();
    }

    // 7. Средний чек по залам
    public void avgTicketPriceByHall() {
        printHeader("Запрос 7: Средний чек по залам");
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            List<Object[]> results = em.createQuery("""
                    SELECT t.screening.hall.name,
                           COUNT(t),
                           AVG(t.screening.basePrice * t.seat.priceCoefficient)
                    FROM Ticket t
                    GROUP BY t.screening.hall.name
                    ORDER BY AVG(t.screening.basePrice * t.seat.priceCoefficient) DESC
                    """, Object[].class).getResultList();

            System.out.printf("     %-16s %-10s %-15s%n", "Зал", "Билетов", "Средний чек");
            System.out.println("     " + "─".repeat(43));
            for (Object[] row : results) {
                System.out.printf("     %-16s %-10d %-15.2f%n", row[0], (long) row[1], (double) row[2]);
            }
        }
        printDivider();
    }

    // 8. Посетители без билетов
    public void visitorsWithoutTickets() {
        printHeader("Запрос 8: Посетители без билетов");
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            // Подзапрос в JPQL - аналог NOT EXISTS в SQL
            List<Object[]> results = em.createQuery("""
                    SELECT v.name, v.email
                    FROM Visitor v
                    WHERE v NOT IN (SELECT t.visitor FROM Ticket t)
                    ORDER BY v.name
                    """, Object[].class).getResultList();

            System.out.printf("     %-25s %-25s%n", "Имя", "Email");
            System.out.println("     " + "─".repeat(52));
            if (results.isEmpty()) {
                System.out.println("     (все посетители имеют билеты)");
            }
            for (Object[] row : results) {
                System.out.printf("     %-25s %-25s%n", row[0], row[1] != null ? row[1] : "—");
            }
        }
        printDivider();
    }

    // 9. Свободные места на сеанс
    public void freeSeatsForScreening(int screeningId) {
        printHeader("Свободные места на сеанс id=" + screeningId);
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            List<Object[]> results = em.createQuery("""
                    SELECT s.id.seatNumber, s.rowNumber, s.priceCoefficient
                    FROM Seat s
                    WHERE s.hall = (SELECT sc.hall FROM Screening sc WHERE sc.id = :sId)
                      AND s NOT IN (SELECT t.seat FROM Ticket t WHERE t.screening.id = :sId)
                    ORDER BY s.id.seatNumber
                    """, Object[].class)
                    .setParameter("sId", screeningId)
                    .getResultList();

            System.out.printf("     %-8s %-8s %-12s%n", "Место", "Ряд", "Коэфф.");
            System.out.println("     " + "─".repeat(30));
            for (Object[] row : results) {
                System.out.printf("     %-8d %-8d %-12s%n", (short) row[0], (short) row[1], row[2]);
            }
            System.out.printf("     Свободно мест: %d%n", results.size());
        }
        printDivider();
    }
 
    // 10. Статистика по фильмам: название, сеансов, билетов
    public void filmStatistics() {
        printHeader("Общая статистика по фильмам");
        try (EntityManager em = HibernateUtil.createEntityManager()) {
            List<Object[]> results = em.createQuery("""
                    SELECT f.title,
                           (SELECT COUNT(s) FROM Screening s WHERE s.film = f),
                           (SELECT COUNT(t) FROM Ticket t WHERE t.screening.film = f)
                    FROM Film f
                    ORDER BY f.title
                    """, Object[].class).getResultList();

            System.out.printf("     %-22s %-10s %-10s%n", "Фильм", "Сеансов", "Билетов");
            System.out.println("     " + "─".repeat(44));
            for (Object[] row : results) {
                System.out.printf("     %-22s %-10d %-10d%n", row[0], (long) row[1], (long) row[2]);
            }
        }
        printDivider();
    }

    public void runAll() {
        revenueByFilm();
        hallOccupancy();
        topVisitors();
        filmsWithGenreCount();
        screeningsWithDetails();
        genrePopularity();
        avgTicketPriceByHall();
        visitorsWithoutTickets();
        freeSeatsForScreening(1);
        filmStatistics();
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
