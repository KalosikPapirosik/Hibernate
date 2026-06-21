package com.cinema.service.щдв;

import com.cinema.entity.old.*;
import com.cinema.repository.GenericRepository;
import com.cinema.repository.old.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

/**
 * Демонстрация CRUD-операций через Hibernate Repository.
 */
public class CrudDemoService {

    private final VisitorRepository visitorRepo = new VisitorRepository();
    private final FilmRepository filmRepo = new FilmRepository();
    private final SeatRepository seatRepo = new SeatRepository();
    private final ScreeningRepository screeningRepo = new ScreeningRepository();
    private final TicketRepository ticketRepo = new TicketRepository();
    private final GenericRepository<Genre, Integer> genreRepo = new GenericRepository<>(Genre.class);
    private final GenericRepository<Hall, Short> hallRepo = new GenericRepository<>(Hall.class);


    public void demoCreate() {
        printHeader("CREATE — Создание записей");

        Visitor visitor = visitorRepo.save(new Visitor("Тестов Тест", "+79991112233", "test@test.ru"));
        System.out.printf("Создан посетитель: id=%d, %s%n", visitor.getId(), visitor.getName());

        Film film = filmRepo.save(new Film("Тестовый Фильм", LocalTime.of(1, 45)));
        System.out.printf("Создан фильм: id=%d, '%s', %s%n", film.getId(), film.getTitle(), film.getDuration());

        filmRepo.addGenre(film.getId(), 1);
        filmRepo.addGenre(film.getId(), 2);
        Film filmWithGenres = filmRepo.findByIdWithGenres(film.getId()).orElseThrow();
        System.out.printf("Привязаны жанры: %s%n",
                filmWithGenres.getGenres().stream().map(Genre::getName).toList());

        Hall hall = hallRepo.findById((short) 1).orElseThrow();
        Screening screening = screeningRepo.save(new Screening(
                hall, film,
                OffsetDateTime.of(2026, 5, 10, 20, 0, 0, 0, ZoneOffset.ofHours(3)),
                BigDecimal.valueOf(500)
        ));
        System.out.printf("Создан сеанс: id=%d, зал=%s, %s, цена=500₽%n",
                screening.getId(), hall.getName(), screening.getDateTime());

        printDivider();
    }

    public void demoRead() {
        printHeader("READ — Чтение данных");

        System.out.println("Все посетители:");
        List<Visitor> visitors = visitorRepo.findAll();
        System.out.printf("     %-5s %-20s %-16s %-25s%n", "ID", "Имя", "Телефон", "Email");
        System.out.println("     " + "─".repeat(68));
        for (Visitor v : visitors) {
            System.out.printf("     %-5d %-20s %-16s %-25s%n",
                    v.getId(), v.getName(), v.getPhone(), v.getEmail());
        }

        System.out.println("\nФильмы с жанрами:");
        List<Film> films = filmRepo.findAllWithGenres();
        System.out.printf("     %-5s %-22s %-14s %-30s%n", "ID", "Название", "Длительность", "Жанры");
        System.out.println("     " + "─".repeat(73));
        for (Film f : films) {
            String genres = f.getGenres().stream().map(Genre::getName).toList().toString();
            System.out.printf("     %-5d %-22s %-14s %-30s%n",
                    f.getId(), f.getTitle(), f.getDuration(), genres);
        }

        System.out.println("\nВсе залы:");
        List<Hall> halls = hallRepo.findAll();
        System.out.printf("     %-5s %-16s %-13s %-10s%n", "№", "Название", "Вместимость", "Тип");
        System.out.println("     " + "─".repeat(46));
        for (Hall h : halls) {
            System.out.printf("     %-5d %-16s %-13d %-10s%n",
                    h.getId(), h.getName(), h.getCapacity(), h.getTypeName());
        }

        System.out.println("\nПоиск посетителя по id=1:");
        visitorRepo.findById(1).ifPresentOrElse(
                v -> System.out.println("     " + v),
                () -> System.out.println("     Не найден")
        );

        System.out.println("\nПоиск места: место=5, зал=1:");
        seatRepo.findById(new SeatId((short) 5, (short) 1)).ifPresentOrElse(
                s -> System.out.println("     " + s),
                () -> System.out.println("     Не найдено")
        );

        printDivider();
    }

     public void demoUpdate() {
        printHeader("UPDATE — Обновление данных");

        visitorRepo.findById(1).ifPresent(v -> {
            String oldEmail = v.getEmail();
            v.setEmail("updated@mail.ru");
            Visitor updated = visitorRepo.update(v);
            System.out.printf("  Обновлён email id=1: '%s' → '%s'%n", oldEmail, updated.getEmail());
            updated.setEmail(oldEmail);
            visitorRepo.update(updated);
        });

        printDivider();
    }

    public void demoDelete() {
        printHeader("DELETE — Удаление данных");

        Visitor temp = visitorRepo.save(new Visitor("Удали Меня", "+70000000000", "delete@me.ru"));
        System.out.printf("Создан временный: id=%d%n", temp.getId());

        boolean deleted = visitorRepo.deleteById(temp.getId());
        System.out.printf(" Удалён id=%d (успех=%b)%n", temp.getId(), deleted);

        boolean notFound = visitorRepo.deleteById(99999);
        System.out.printf(" Удаление несуществующего id=99999 (успех=%b)%n", notFound);

        printDivider();
    }

    public void demoTransaction() {
        printHeader("TRANSACTION — Покупка билета");

        System.out.println("Покупка: посетитель=1, сеанс=1, место=1, зал=1");
        try {
            int ticketId = ticketRepo.purchaseTicket(1, 1, (short) 1, (short) 1);
            System.out.printf("Билет куплен! id=%d%n", ticketId);

            System.out.println("\nПовторная покупка того же места...");
            try {
                ticketRepo.purchaseTicket(2, 1, (short) 1, (short) 1);
            } catch (IllegalStateException e) {
                System.out.printf("Ожидаемая ошибка: %s%n", e.getMessage());
            }

            ticketRepo.deleteById(ticketId);
        } catch (Exception e) {
            System.out.printf("%s%n", e.getMessage());
        }

        printDivider();
    }


    public void runAll() {
        demoRead();
        demoCreate();
        demoUpdate();
        demoDelete();
        demoTransaction();
    }


    public static void printHeader(String title) {
        System.out.println();
        System.out.println("╔" + "═".repeat(title.length() + 4) + "╗");
        System.out.println("║  " + title + "  ║");
        System.out.println("╚" + "═".repeat(title.length() + 4) + "╝");
    }

    public static void printDivider() {
        System.out.println("─".repeat(80));
    }
}
