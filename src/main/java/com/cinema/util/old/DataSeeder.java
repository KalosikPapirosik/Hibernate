package com.cinema.util.old;

import com.cinema.entity.old.Film;
import com.cinema.entity.old.Genre;
import com.cinema.entity.old.Hall;
import com.cinema.entity.old.Screening;
import com.cinema.entity.old.Seat;
import com.cinema.entity.old.SeatId;
import com.cinema.entity.old.Ticket;
import com.cinema.entity.old.Visitor;
import com.cinema.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

public final class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private DataSeeder() {}

    public static void seed() {
        EntityManager em = HibernateUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            Long visitorsCount = em.createQuery("SELECT COUNT(v) FROM Visitor v", Long.class)
                    .getSingleResult();
            if (visitorsCount > 0) {
                tx.commit();
                log.info("Начальные данные уже есть, заполнение пропущено");
                return;
            }

            Genre action = new Genre("Боевик");
            Genre drama = new Genre("Драма");
            Genre sciFi = new Genre("Фантастика");
            Genre comedy = new Genre("Комедия");
            List.of(action, drama, sciFi, comedy).forEach(em::persist);

            Hall hall1 = new Hall("Большой зал", (short) 15, (short) 1);
            Hall hall2 = new Hall("VIP зал", (short) 12, (short) 2);
            Hall hall3 = new Hall("IMAX зал", (short) 8, (short) 3);
            List.of(hall1, hall2, hall3).forEach(em::persist);
            em.flush();

            persistSeats(em, hall1, 15);
            persistSeats(em, hall2, 12);
            persistSeats(em, hall3, 8);

            Film matrix = new Film("Матрица", LocalTime.of(2, 16));
            matrix.addGenre(action);
            matrix.addGenre(sciFi);

            Film greenMile = new Film("Зеленая миля", LocalTime.of(3, 9));
            greenMile.addGenre(drama);

            Film interstellar = new Film("Интерстеллар", LocalTime.of(2, 49));
            interstellar.addGenre(sciFi);
            interstellar.addGenre(drama);

            Film gentlemen = new Film("Джентльмены", LocalTime.of(1, 53));
            gentlemen.addGenre(action);
            gentlemen.addGenre(comedy);

            Film backToFuture = new Film("Назад в будущее", LocalTime.of(1, 56));
            backToFuture.addGenre(sciFi);
            backToFuture.addGenre(comedy);

            List.of(matrix, greenMile, interstellar, gentlemen, backToFuture).forEach(em::persist);

            OffsetDateTime baseDate = OffsetDateTime.of(2026, 5, 20, 12, 0, 0, 0, ZoneOffset.ofHours(3));
            Screening screening1 = new Screening(hall1, matrix, baseDate.plusHours(2), new BigDecimal("450.00"));
            Screening screening2 = new Screening(hall2, greenMile, baseDate.plusHours(5), new BigDecimal("700.00"));
            Screening screening3 = new Screening(hall1, interstellar, baseDate.plusDays(1).plusHours(1), new BigDecimal("500.00"));
            Screening screening4 = new Screening(hall3, gentlemen, baseDate.plusDays(1).plusHours(4), new BigDecimal("850.00"));
            Screening screening5 = new Screening(hall2, backToFuture, baseDate.plusDays(2).plusHours(3), new BigDecimal("650.00"));
            List.of(screening1, screening2, screening3, screening4, screening5).forEach(em::persist);

            Visitor visitor1 = new Visitor("Иван Петров", "+79001234567", "ivan@mail.ru");
            Visitor visitor2 = new Visitor("Анна Смирнова", "+79007654321", "anna@mail.ru");
            Visitor visitor3 = new Visitor("Олег Иванов", "+79001112233", "oleg@mail.ru");
            Visitor visitor4 = new Visitor("Мария Кузнецова", "+79004445566", "maria@mail.ru");
            Visitor visitor5 = new Visitor("Дмитрий Соколов", "+79007778899", "dmitry@mail.ru");
            Visitor visitor6 = new Visitor("Свободный Гость", "+79009990011", "guest@mail.ru");
            List.of(visitor1, visitor2, visitor3, visitor4, visitor5, visitor6).forEach(em::persist);

            em.flush();

            em.persist(new Ticket(visitor1, screening1, em.find(Seat.class, new SeatId((short) 2, hall1.getId()))));
            em.persist(new Ticket(visitor2, screening1, em.find(Seat.class, new SeatId((short) 3, hall1.getId()))));
            em.persist(new Ticket(visitor3, screening2, em.find(Seat.class, new SeatId((short) 1, hall2.getId()))));
            em.persist(new Ticket(visitor1, screening3, em.find(Seat.class, new SeatId((short) 4, hall1.getId()))));
            em.persist(new Ticket(visitor4, screening4, em.find(Seat.class, new SeatId((short) 1, hall3.getId()))));
            em.persist(new Ticket(visitor5, screening5, em.find(Seat.class, new SeatId((short) 5, hall2.getId()))));

            tx.commit();
            log.info("Начальные данные для Hibernate demo добавлены");
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private static void persistSeats(EntityManager em, Hall hall, int count) {
        for (short seatNumber = 1; seatNumber <= count; seatNumber++) {
            short row = (short) (((seatNumber - 1) / 5) + 1);
            short seatType = seatNumber <= 2 ? (short) 2 : (short) 1;
            BigDecimal coefficient = seatType == 2 ? new BigDecimal("1.30") : BigDecimal.ONE;
            em.persist(new Seat(seatNumber, hall, row, seatType, coefficient));
        }
    }
}
