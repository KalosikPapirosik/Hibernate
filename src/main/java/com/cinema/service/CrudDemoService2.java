package com.cinema.service;

import com.cinema.entity.*;
import com.cinema.repository.*;
import com.cinema.util.DataSeeder;
import com.cinema.util.HibernateUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Интерактивный консольный интерфейс, использующий кастомные репозитории
 * для расширенного CRUD и аналитики.
 */
public class CrudDemoService2 {

    private static final Scanner sc = new Scanner(System.in);
    private static final BusinessQueryService business = new BusinessQueryService();

    // ── Кастомные репозитории ──────────────────────────────────
    private final GuestRepository guestRepo = new GuestRepository();
    private final HousingRepository housingRepo = new HousingRepository();
    private final CampRepository campRepo = new CampRepository();
    private final BookingRepository bookingRepo = new BookingRepository();
    private final InventoryRepository inventoryRepo = new InventoryRepository();
    private final AdditionalServicesRepository serviceRepo = new AdditionalServicesRepository();

    public static void main(String[] args) {
        new CrudDemoService2().start();
    }

    public void start() {
        System.out.println("Добро пожаловать в систему управления кемпингами");
        boolean running = true;

        while (running) {
            printHeader("ГЛАВНОЕ МЕНЮ");
            System.out.println("  1. CRUD-операции (через кастомные репозитории)");
            System.out.println("  2. Расширенный поиск (спец. запросы репозиториев)");
            System.out.println("  3. Бизнес-запросы (аналитика)");
            System.out.println("  4. Заполнить БД демо-данными");
            System.out.println("  0. Выход");
            System.out.println("─".repeat(55));

            switch (readInt("Выберите действие: ")) {
                case 1 -> runCrudMenu();
                case 2 -> runSpecialQueriesMenu();
                case 3 -> runBusinessMenu();
                case 4 -> runWithEm(em -> DataSeeder.seed(), "Демо-данные успешно добавлены");
                case 0 -> { running = false; System.out.println("Сеанс завершён."); }
                default -> System.out.println("Неверный ввод. Попробуйте снова.\n");
            }
        }
    }

    // =================================================================
    // МЕНЮ CRUD (БАЗОВЫЕ ОПЕРАЦИИ)
    // =================================================================
    private void runCrudMenu() {
        boolean back = false;
        while (!back) {
            printHeader("CRUD-ОПЕРАЦИИ");
            System.out.println("  1. Создать запись");
            System.out.println("  2. Просмотреть все записи");
            System.out.println("  3. Обновить запись");
            System.out.println("  4. Удалить запись");
            System.out.println("  0. Назад");
            System.out.println("─".repeat(50));

            switch (readInt("Выберите действие: ")) {
                case 1 -> runCreateMenu();
                case 2 -> runReadAllMenu();
                case 3 -> runUpdateMenu();
                case 4 -> runDeleteMenu();
                case 0 -> back = true;
                default -> System.out.println("Неверный ввод.");
            }
        }
    }

    // =================================================================
    // МЕНЮ СПЕЦ. ЗАПРОСОВ (JOIN FETCH, ФИЛЬТРАЦИЯ)
    // =================================================================
    private void runSpecialQueriesMenu() {
        boolean back = false;
        while (!back) {
            printHeader("РАСШИРЕННЫЙ ПОИСК");
            System.out.println("  1. Гости с бронированиями");
            System.out.println("  2. Жильё с типом и условиями");
            System.out.println("  3. Бронирование по Id с деталями");
            System.out.println("  4. Кемпы с инвентарем");
            System.out.println("  5. Бронирования гостя с деталями");
            System.out.println("  6. Поиск жилья по типу");
            System.out.println("  0. Назад");
            System.out.println("─".repeat(50));

            switch (readInt("Выберите запрос: ")) {
                case 1 -> printList(guestRepo.findAllWithBooking(), "Гости с бронированиями");
                case 2 -> {
                    int id = readInt("ID жилья: ");
                    housingRepo.findByIdWithDetails(id)
                            .ifPresentOrElse(h -> System.out.println("\n" + h + "\n"),
                                    () -> System.out.println("Не найдено"));
                    pause();
                }
                case 3 -> {
                    System.out.println("Бронирование с деталями:");
                    int id = readInt("Введите id бронирования: ");
                    Optional<Booking> optBooking = bookingRepo.findByIdWithDetails(id); // ← замените 1 на нужный ID

                    System.out.printf("%-5s %-24s %-20s %-22s %-14s%n",
                            "ID", "Гость", "Жильё / Кемп", "Период", "Стоимость");
                    System.out.println("─".repeat(87));

                    optBooking.ifPresentOrElse(
                            b -> {
                                String guest = (b.getGuest() != null)
                                        ? b.getGuest().getLastname() + " " + b.getGuest().getFirstname()
                                        : "—";

                                String housing = (b.getHousing() != null && b.getHousing().getType() != null)
                                        ? b.getHousing().getType().getName()
                                        : "—";
                                String camp = (b.getHousing() != null && b.getHousing().getCamp() != null)
                                        ? "(" + b.getHousing().getCamp().getLocation() + ")"
                                        : "";

                                String period = (b.getDateOfStart() != null && b.getDateOfEnd() != null)
                                        ? b.getDateOfStart().toLocalDate() + " — " + b.getDateOfEnd().toLocalDate()
                                        : "—";

                                String price = (b.getHousing() != null && b.getHousing().getCost() != null)
                                        ? String.format("%.2f ₽", b.getHousing().getCost())
                                        : "—";

                                System.out.printf("%-5d %-24s %-20s %-22s %-14s%n",
                                        b.getId(), guest, housing + " " + camp, period, price);
                            },
                            () -> System.out.println("Бронирование не найдено")
                    );
                }
                case 4 -> {
                    System.out.println("Кемпы с инвентарём:");
                    List<Camp> camps = campRepo.findAllWithInventory();
                    System.out.printf("%-5s %-28s %-40s%n", "ID", "Локация", "Инвентарь");
                    System.out.println("     " + "─".repeat(75));
                    for (Camp c : camps) {
                        String inventory = (c.getInventory() != null && !c.getInventory().isEmpty())
                                ? c.getInventory().stream()
                                .map(inv -> inv.getTypeOfInventory() + " (x" + inv.getQuantity() + ")")
                                .toList().toString()
                                : "—";

                        System.out.printf("%-5d %-28s %-40s%n",
                                c.getId(),
                                c.getLocation() != null ? c.getLocation() : "—",
                                inventory);
                    }
                }
                case 5 -> {
                    System.out.println("Бронирования гостя ");
                    int guestId = readInt("ID гостя: ");
                    List<Booking> bookings = bookingRepo.findByGuestIdWithDetails(guestId);

                    System.out.printf("%-5s %-20s %-24s %-26s %-14s %-12s%n",
                            "ID", "Гость", "Жильё / Кемп", "Период", "Доп. услуга", "Стоимость");
                    System.out.println("     " + "─".repeat(103));

                    if (bookings.isEmpty()) {
                        System.out.println("У гостя нет бронирований");
                    } else {
                        for (Booking b : bookings) {
                            String guestName = (b.getGuest() != null)
                                    ? b.getGuest().getLastname() + " " + b.getGuest().getFirstname()
                                    : "—";

                            String housingInfo = (b.getHousing() != null && b.getHousing().getType() != null)
                                    ? b.getHousing().getType().getName() + " (" +
                                    (b.getHousing().getCamp() != null ? b.getHousing().getCamp().getLocation() : "?") + ")"
                                    : "—";

                            String period = (b.getDateOfStart() != null && b.getDateOfEnd() != null)
                                    ? b.getDateOfStart().toLocalDate() + " → " + b.getDateOfEnd().toLocalDate()
                                    : "—";

                            String service = (b.getAdditionalService() != null)
                                    ? b.getAdditionalService().getLabel()
                                    : "—";

                            String price = (b.getHousing() != null && b.getHousing().getCost() != null)
                                    ? String.format("%.2f ₽", b.getHousing().getCost())
                                    : "—";

                            System.out.printf("%-5d %-20s %-24s %-26s %-14s %-12s%n",
                                    b.getId(), guestName, housingInfo, period, service, price);
                        }
                    }
                }
                case 6 -> {
                    System.out.println("Жильё по ID типа ");
                    int typeId = readInt("ID типа жилья: ");
                    List<Housing> housings = housingRepo.findByTypeId(typeId);

                    System.out.printf("%-5s %-18s %-10s %-24s %-16s %-12s%n",
                            "ID", "Тип", "Вмест.", "Кемп", "Комфорт", "Цена (₽)");
                    System.out.println("     " + "─".repeat(85));

                    if (housings.isEmpty()) {
                        System.out.println("Жильё данного типа не найдено");
                    } else {
                        for (Housing h : housings) {
                            String type = h.getType() != null ? h.getType().getName() : "—";
                            String camp = h.getCamp() != null ? h.getCamp().getLocation() : "—";
                            String cost = h.getCost() != null ? String.format("%.2f", h.getCost()) : "—";

                            System.out.printf("%-5d %-18s %-10d %-24s %-16s %-12s%n",
                                    h.getId(),
                                    type,
                                    h.getCapacity() != null ? h.getCapacity() : 0,
                                    camp,
                                    h.getComfortLvl() != null ? h.getComfortLvl() : "—",
                                    cost);
                        }
                    }
                }
                case 0 -> back = true;
                default -> System.out.println("Неверный ввод.");
            }
        }
    }

    // =================================================================
    // МЕНЮ БИЗНЕС-ЗАПРОСОВ (АНАЛИТИКА)
    // =================================================================
    private void runBusinessMenu() {
        boolean back = false;
        while (!back) {
            printHeader("БИЗНЕС-ЗАПРОСЫ");
            System.out.println("  1. Выручка по типам жилья");
            System.out.println("  2. Заполняемость кемпов");
            System.out.println("  3. Топ-5 гостей по тратам");
            System.out.println("  4. Популярность доп. услуг");
            System.out.println("  5. Динамика бронирований (30 дней)");
            System.out.println("  6. Выполнить все подряд");
            System.out.println("  0. Назад");
            System.out.println("─".repeat(50));

            int choice = readInt("Выберите запрос: ");
            Runnable[] queries = {
                    business::revenueByHousingType,
                    business::campOccupancy,
                    business::topGuestsBySpending,
                    business::additionalServicesPopularity,
                    business::bookingTrendsLastMonth
            };

            if (choice >= 1 && choice <= 5) {
                runWithEm(em -> queries[choice - 1].run(), "Запрос выполнен");
            } else if (choice == 6) {
                runWithEm(em -> business.runAll(), "Все бизнес-запросы выполнены");
            } else if (choice == 0) {
                back = true;
            } else {
                System.out.println("Неверный ввод.");
            }
        }
    }

    // =================================================================
    // CRUD: СОЗДАНИЕ
    // =================================================================
    private void runCreateMenu() {
        printHeader("РЕЖИМ СОЗДАНИЯ");
        System.out.println("  1. Ручной ввод данных");
        System.out.println("  2. Тестовые данные (авто)");
        boolean mode = readInt("Выберите режим (1/2): ") == 1;

        System.out.println("Какую сущность создать?");
        System.out.println("  1. Гость (Guest)");
        System.out.println("  2. Кемп (Camp)");
        System.out.println("  3. Жильё (Housing)");
        System.out.println("  4. Бронирование (Booking)");
        System.out.println("  5. Доп. услуга (AdditionalServices)");
        System.out.println("  6. Инвентарь (Inventory)");
        System.out.println("  0. Назад");

        switch (readInt("Сущность: ")) {
            case 1 -> createGuest(mode);
            case 2 -> createCamp(mode);
            case 3 -> createHousing(mode);
            case 4 -> createBooking(mode);
            case 5 -> createService(mode);
            case 6 -> createInventory(mode);
        }
    }

    private void createGuest(boolean manual) {
        String doc, last, first, patr;
        if (manual) {
            doc = readNonEmptyLine("Документ: ");
            last = readNonEmptyLine("Фамилия: ");
            first = readNonEmptyLine("Имя: ");
            patr = sc.nextLine().trim();
            patr = patr.isEmpty() ? null : patr;
        } else {
            long ts = System.currentTimeMillis() % 10000;
            doc = "DOC-" + ts; last = "TestLast"; first = "TestFirst"; patr = "TestPatronymic";
        }
        Guest g = (Guest) guestRepo.save(new Guest(doc, first, last, patr));
        System.out.println("Гость создан. ID: " + g.getId());
        pause();
    }

    private void createCamp(boolean manual) {
        String loc = manual ? readNonEmptyLine("Локация: ") : "Test Camp " + (System.currentTimeMillis() % 100);
        Camp c = campRepo.save(new Camp(loc));
        System.out.println("Кемп создан. ID: " + c.getId());
        pause();
    }

    private void createHousing(boolean manual) {
        int typeId = readInt("ID типа жилья: ");
        int campId = readInt("ID кемпа: ");
        int condId = readInt("ID условия: ");
        String comfort = manual ? readNonEmptyLine("Уровень комфорта: ") : "Комфорт ++";
        int cap = manual ? readInt("Вместимость: ") : 4;
        BigDecimal cost = manual ? readBigDecimal("Стоимость: ") : new BigDecimal("2500.00");

        runWithEm(em -> {
            TypeOfHousing type = em.getReference(TypeOfHousing.class, typeId);
            Camp camp = em.getReference(Camp.class, campId);
            HousingCondition cond = em.getReference(HousingCondition.class, condId);
            if (!em.contains(type) || !em.contains(camp) || !em.contains(cond))
                throw new IllegalArgumentException("Связанная сущность не найдена");
            Housing h = new Housing(type, camp, comfort, cap, cost, cond);
            em.persist(h);
            System.out.println("Жильё создано. ID: " + h.getId());
        }, "Жильё сохранено");
    }

    private void createBooking(boolean manual) {
        int guestId = readInt("ID гостя: ");
        int housingId = readInt("ID жилья: ");
        Integer serviceId = null;
        System.out.print("ID доп.услуги (Enter если нет): ");
        String svcInput = sc.nextLine().trim();
        if (!svcInput.isEmpty()) serviceId = Integer.parseInt(svcInput);

        OffsetDateTime start, end, serviceDate = null;
        if (manual) {
            start = readOffsetDateTime("Дата начала: ");
            end = readOffsetDateTime("Дата окончания: ");
            if (!end.isAfter(start)) throw new IllegalArgumentException("⚠Дата окончания должна быть позже");
            System.out.print("Дата оказания услуги (Enter если нет): ");
            String sd = sc.nextLine().trim();
            if (!sd.isEmpty()) serviceDate = OffsetDateTime.parse(sd);
        } else {
            start = OffsetDateTime.now();
            end = start.plusDays(5);
        }

        int bookingId = bookingRepo.createBooking(guestId, housingId, serviceId, start, end, serviceDate);
        System.out.println("Бронирование создано. ID: " + bookingId);
        pause();
    }

    private void createService(boolean manual) {
        String label = manual ? readNonEmptyLine("Название услуги: ") : "Тестовая услуга";
        BigDecimal cost = manual ? readBigDecimal("Стоимость: ") : new BigDecimal("500.00");
        AdditionalServices s = (AdditionalServices) serviceRepo.save(new AdditionalServices(label, cost));
        System.out.println("Услуга создана. ID: " + s.getId());
        pause();
    }

    private void createInventory(boolean manual) {
        int campId = readInt("ID кемпа: ");
        String type = manual ? readNonEmptyLine("Тип инвентаря: ") : "Тестовый инвентарь";
        int qty = manual ? readInt("Количество: ") : 10;

        runWithEm(em -> {
            Camp camp = em.find(Camp.class, campId);
            if (camp == null) throw new IllegalArgumentException("⚠Кемп не найден");
            Inventory inv = new Inventory(camp, type, qty);
            em.persist(inv);
            System.out.println("Инвентарь создан. ID: " + inv.getId());
        }, "Инвентарь сохранён");
    }

    // =================================================================
    // CRUD: ЧТЕНИЕ / ОБНОВЛЕНИЕ / УДАЛЕНИЕ
    // =================================================================
    private void runReadAllMenu() {
        printHeader("ПРОСМОТР ВСЕХ ЗАПИСЕЙ");
        System.out.println(" 1. Гости  \n 2. Кемпы \n 3. Жильё \n 4. Бронирования \n 5. Услуги \n 6. Инвентарь \n 0. Назад");

        switch (readInt("Выберите: ")) {
            case 1 -> printList(guestRepo.findAll(), "Гости");
            case 2 -> printList(campRepo.findAll(), "Кемпы");
            case 3 -> printList(housingRepo.findAll(), "Жильё");
            case 4 -> printList(bookingRepo.findAll(), "Бронирования");
            case 5 -> printList(serviceRepo.findAll(), "Доп. услуги");
            case 6 -> printList(inventoryRepo.findAll(), "Инвентарь");
        }
    }

    private void runUpdateMenu() {
        System.out.println("Что обновить?");
        System.out.println(" 1. Документ гостя \n 2. Вместимость жилья \n 3. Стоимость услуги \n 0. Назад");
        switch (readInt("Поле: ")) {
            case 1 -> {
                int id = readInt("ID гостя: ");
                Guest g = null;
                try {
                    g = (Guest) guestRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Не найден"));
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                g.setDocument(readNonEmptyLine("Новый документ: "));
                guestRepo.update(g);
                System.out.println("Обновлено");
            }
            case 2 -> {
                int id = readInt("ID жилья: ");
                Housing h = null;
                try {
                    h = (Housing) housingRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Не найдено"));
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                h.setCapacity(readInt("Новая вместимость: "));
                housingRepo.update(h);
                System.out.println("✅ Обновлено");
            }
            case 3 -> {
                int id = readInt("ID услуги: ");
                AdditionalServices s = null;
                try {
                    s = (AdditionalServices) serviceRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Не найдено"));
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                s.setCost(readBigDecimal("Новая стоимость: "));
                serviceRepo.update(s);
                System.out.println("Обновлено");
            }
        }
        pause();
    }

    private void runDeleteMenu() {
        int id = readInt("ID сущности для удаления: ");
        System.out.println("Тип: 1=Guest 2=Camp 3=Housing 4=Booking 5=Service 6=Inventory 0=Назад");
        switch (readInt("Тип: ")) {
            case 1 -> delete(guestRepo, id, "Гость");
            case 2 -> delete(campRepo, id, "Кемп");
            case 3 -> delete(housingRepo, id, "Жильё");
            case 4 -> delete(bookingRepo, id, "Бронирование");
            case 5 -> delete(serviceRepo, id, "Услуга");
            case 6 -> delete(inventoryRepo, id, "Инвентарь");
        }
    }

    private <T> void delete(GenericRepository<T, Integer> repo, int id, String name) {
        boolean ok = repo.deleteById(id);
        System.out.println(ok ? "Успех " + name + " #" + id + " удалён" : "Не найден");
        pause();
    }

    // =================================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // =================================================================
    private <T> void printList(List<T> list, String title) {
        System.out.println("\n📋 " + title + " (" + list.size() + "):");
        if (list.isEmpty()) System.out.println("  (пусто)");
        else list.forEach(e -> System.out.println("  " + e));
        System.out.println("─".repeat(50));
        pause();
    }

    private void runWithEm(java.util.function.Consumer<EntityManager> action, String successMsg) {
        EntityManager em = HibernateUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            action.accept(em);
            tx.commit();
            System.out.println("Успешно" + successMsg + "\n");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            System.out.println("Ошибка: " + e.getMessage() + "\n");
        } finally {
            em.close();
        }
        pause();
    }

    private String readNonEmptyLine(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (!input.isEmpty()) return input;
            System.out.println("⚠️ Поле не может быть пустым.");
        }
    }

    private OffsetDateTime readOffsetDateTime(String prompt) {
        System.out.println(prompt + " (формат: YYYY-MM-DDTHH:MM:SS+03:00 | 'текущая' или Enter)");
        while (true) {
            String input = sc.nextLine().trim();
            if (input.isEmpty() || input.equalsIgnoreCase("текущая") || input.equalsIgnoreCase("now"))
                return OffsetDateTime.now();
            try {
                return OffsetDateTime.parse(input);
            } catch (DateTimeParseException e) {
                System.out.println("⚠️ Неверный формат. Пример: 2026-07-10T14:30:00+03:00");
            }
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.isEmpty()) { System.out.println("⚠️ Введите число."); continue; }
            try { return Integer.parseInt(input); }
            catch (NumberFormatException e) { System.out.println("⚠️ Неверный формат."); }
        }
    }

    private BigDecimal readBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.nextLine().trim();
            if (input.isEmpty()) { System.out.println("⚠️ Введите число."); continue; }
            try { return new BigDecimal(input); }
            catch (NumberFormatException e) { System.out.println("⚠️ Неверный формат."); }
        }
    }

    private void printHeader(String title) {
        System.out.println("\n╔" + "═".repeat(title.length() + 4) + "╗");
        System.out.println("║  " + title + "  ║");
        System.out.println("╚" + "═".repeat(title.length() + 4) + "╝");
    }

    private void pause() {
        System.out.print("Нажмите Enter для возврата...");
        sc.nextLine();
    }
}