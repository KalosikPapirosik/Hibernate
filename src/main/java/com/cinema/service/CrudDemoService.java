package com.cinema.service;

import com.cinema.entity.*;
import com.cinema.util.HibernateUtil;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

/**
 * Демонстрация CRUD-операций для системы бронирования жилья в кемпингах.
 * Реализовано на чистом Hibernate/JPA через EntityManager.
 */
public class CrudDemoService {

    // ─────────────────────────────────────────────────────
    // CREATE — Создание записей
    // ─────────────────────────────────────────────────────
    public void demoCreate() {
        printHeader("CREATE — Создание записей (Hibernate)");
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = HibernateUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Создаём гостя
            Guest guest = new Guest("Иванов", "Иван", "Иванович", "1234567890");
            em.persist(guest);
            em.flush(); // Генерируем ID для вывода
            System.out.printf("✅ Создан гость: id=%d, %s %s%n",
                    guest.getId(), guest.getFirstname(), guest.getLastname());

            // 2. Создаём жильё (связи через объекты-сущности!)
            TypeOfHousing type = em.getReference(TypeOfHousing.class, 1);
            Camp camp = em.getReference(Camp.class, 1);
            HousingCondition condition = em.getReference(HousingCondition.class, 1);

            Housing housing = new Housing(type, camp, "Люкс", 4, new BigDecimal("2500.00"), condition);
            em.persist(housing);
            em.flush();
            System.out.printf("✅ Создано жильё: id=%d, тип='%s', кемп='%s', цена=%s₽%n",
                    housing.getId(),
                    housing.getType().getName(),
                    housing.getCamp().getLocation(),
                    housing.getCost());

            // 3. Создаём бронирование (связи через объекты!)
            Booking booking = new Booking(
                    guest,
                    housing,
                    null, // доп.услуга опциональна
                    OffsetDateTime.now(),
                    OffsetDateTime.now().plusDays(7),
                    null
            );
            em.persist(booking);
            tx.commit();
            System.out.printf("✅ Создано бронирование: id=%d, период: %s → %s%n",
                    booking.getId(),
                    booking.getDateOfStart().toLocalDate(),
                    booking.getDateOfEnd().toLocalDate());

            // 4. Демонстрация: найти все брони для жилья
            List<Booking> bookings = em.createQuery(
                            "SELECT b FROM Booking b WHERE b.housing.id = :housingId", Booking.class)
                    .setParameter("housingId", housing.getId())
                    .getResultList();
            System.out.printf("📋 Бронирований для жилья #%d: %d%n", housing.getId(), bookings.size());

        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            System.err.println("❌ Ошибка при создании: " + e.getMessage());
            throw e;
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
        printDivider();
    }

    // ─────────────────────────────────────────────────────
    // READ — Чтение данных
    // ─────────────────────────────────────────────────────
    public void demoRead() {
        printHeader("READ — Чтение данных (Hibernate)");
        EntityManager em = null;
        try {
            em = HibernateUtil.createEntityManager();

            // 1. Все гости
            System.out.println("👥 Все гости:");
            System.out.printf("     %-5s %-12s %-15s %-15s %-15s%n", "ID", "Документ", "Фамилия", "Имя", "Отчество");
            System.out.println("     " + "─".repeat(70));
            em.createQuery("SELECT g FROM Guest g ORDER BY g.id", Guest.class)
                    .getResultList()
                    .forEach(g -> System.out.printf("     %-5d %-12s %-15s %-15s %-15s%n",
                            g.getId(), g.getDocument(), g.getLastname(), g.getFirstname(),
                            g.getPatronymic() != null ? g.getPatronymic() : "-"));

            // 2. Жильё с подгрузкой всех связей (FETCH JOIN → 1 запрос!)
            System.out.println("\n🏠 Все жильё с деталями:");
            System.out.printf("     %-5s %-12s %-10s %-10s %-15s %-10s%n",
                    "ID", "Тип", "Кемп", "Вмест.", "Цена", "Условия");
            System.out.println("     " + "─".repeat(75));
            em.createQuery(
                            "SELECT DISTINCT h FROM Housing h " +
                                    "LEFT JOIN FETCH h.type " +
                                    "LEFT JOIN FETCH h.camp " +
                                    "LEFT JOIN FETCH h.condition " +
                                    "ORDER BY h.id", Housing.class)
                    .getResultList()
                    .forEach(h -> System.out.printf("     %-5d %-12s %-10s %-10d %-15s %-10s%n",
                            h.getId(),
                            h.getType().getName(),
                            h.getCamp().getLocation(),
                            h.getCapacity(),
                            h.getCost(),
                            h.getCondition().getName()));

            // 3. Все кемпы
            System.out.println("\n⛺ Все кемпы:");
            em.createQuery("SELECT c FROM Camp c ORDER BY c.id", Camp.class)
                    .getResultList()
                    .forEach(c -> System.out.printf("     #%d — %s%n", c.getId(), c.getLocation()));

            // 4. Поиск гостя по ID
            System.out.println("\n🔍 Поиск гостя по id=1:");
            Guest found = em.find(Guest.class, 1);
            if (found != null) {
                System.out.println("     ✅ Найден: " + found);
            } else {
                System.out.println("     ❌ Не найден");
            }

            // 5. Поиск бронирования по жилью и гостю
            System.out.println("\n🔍 Поиск бронирования: жильё=1, гость=1:");
            Booking booking = em.createQuery(
                            "SELECT b FROM Booking b WHERE b.housing.id = :hId AND b.guest.id = :gId", Booking.class)
                    .setParameter("hId", 1)
                    .setParameter("gId", 1)
                    .getResultList()
                    .stream().findFirst().orElse(null);
            if (booking != null) {
                System.out.println("     ✅ Найдено: " + booking);
            } else {
                System.out.println("     ❌ Не найдено");
            }

        } catch (Exception e) {
            System.err.println("❌ Ошибка при чтении: " + e.getMessage());
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
        printDivider();
    }

    // ─────────────────────────────────────────────────────
    // UPDATE — Обновление данных
    // ─────────────────────────────────────────────────────
    public void demoUpdate() {
        printHeader("UPDATE — Обновление данных (Hibernate)");
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = HibernateUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Обновление гостя (dirty checking — update() не нужен!)
            Guest guest = em.find(Guest.class, 1);
            if (guest != null) {
                String oldDoc = guest.getDocument();
                guest.setDocument("9999999999");
                tx.commit(); // Hibernate сам выполнит UPDATE при коммите
                System.out.printf("✅ Обновлён документ гостя #%d: '%s' → '%s'%n",
                        guest.getId(), oldDoc, guest.getDocument());

                // Возвращаем обратно для чистоты демо
                tx.begin();
                guest.setDocument(oldDoc);
                tx.commit();
            }

            // 2. Обновление жилья
            Housing housing = em.find(Housing.class, 1);
            if (housing != null) {
                Integer oldCap = housing.getCapacity();
                housing.setCapacity(10);
                tx.begin();
                tx.commit();
                System.out.printf("✅ Обновлена вместимость жилья #%d: %d → %d%n",
                        housing.getId(), oldCap, housing.getCapacity());

                // Возвращаем обратно
                tx.begin();
                housing.setCapacity(oldCap);
                tx.commit();
            }

        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            System.err.println("❌ Ошибка при обновлении: " + e.getMessage());
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
        printDivider();
    }

    // ─────────────────────────────────────────────────────
    // DELETE — Удаление данных
    // ─────────────────────────────────────────────────────
    public void demoDelete() {
        printHeader("DELETE — Удаление данных (Hibernate)");
        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = HibernateUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            // 1. Создаём и удаляем временного гостя
            Guest temp = new Guest("0000000000", "Temp", "Delete", "Test");
            em.persist(temp);
            em.flush();
            System.out.printf("✅ Создан временный гость id=%d%n", temp.getId());

            em.remove(temp);
            tx.commit();
            System.out.printf("✅ Удалён гость id=%d (успешно)%n", temp.getId());

            // 2. Попытка удалить несуществующего
            tx.begin();
            Guest notFound = em.find(Guest.class, 99999);
            if (notFound != null) em.remove(notFound);
            tx.commit();
            System.out.printf("✅ Удаление несуществующего id=99999: проигнорировано (не найден)%n");

        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            System.err.println("❌ Ошибка при удалении: " + e.getMessage());
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
        printDivider();
    }

    // ─────────────────────────────────────────────────────
    // TRANSACTION — Бронирование с проверкой пересечений дат
    // ─────────────────────────────────────────────────────
    public void demoTransaction() {
        printHeader("TRANSACTION — Бронирование жилья");
        System.out.println("🎯 Попытка забронировать: гость=1, жильё=1, период: сейчас → +5 дней");

        EntityManager em = null;
        EntityTransaction tx = null;
        try {
            em = HibernateUtil.createEntityManager();
            tx = em.getTransaction();
            tx.begin();

            Guest guest = em.find(Guest.class, 1);
            Housing housing = em.find(Housing.class, 1);

            if (guest == null || housing == null) {
                System.out.println("⚠️ Гость или жильё не найдены. Пропускаем тест.");
                return;
            }

            // 🔒 Блокируем жильё на запись (защита от double-booking)
            em.lock(housing, LockModeType.PESSIMISTIC_WRITE);

            OffsetDateTime start = OffsetDateTime.now();
            OffsetDateTime end = start.plusDays(5);

            // 🔍 Проверка пересечения дат:
            // Новая бронь пересекается с существующей, если:
            // existing.start < new.end AND existing.end > new.start
            Long overlapping = em.createQuery(
                            "SELECT COUNT(b) FROM Booking b WHERE b.housing.id = :hId " +
                                    "AND b.dateOfStart < :end AND b.dateOfEnd > :start", Long.class)
                    .setParameter("hId", housing.getId())
                    .setParameter("start", start)
                    .setParameter("end", end)
                    .getSingleResult();

            if (overlapping > 0) {
                tx.rollback();
                throw new IllegalStateException(
                        String.format("❌ Жильё #%d уже забронировано на период %s — %s",
                                housing.getId(), start.toLocalDate(), end.toLocalDate()));
            }

            // ✅ Создаём бронь
            Booking booking = new Booking(guest, housing, null, start, end, null);
            em.persist(booking);
            tx.commit();
            System.out.printf("✅ Бронь успешна! id=%d, период: %s → %s%n",
                    booking.getId(),
                    booking.getDateOfStart().toLocalDate(),
                    booking.getDateOfEnd().toLocalDate());

            // 🔁 Попытка повторной брони на тот же период (должна упасть)
            System.out.println("\n🔄 Повторная попытка забронировать тот же период...");
            EntityManager em2 = HibernateUtil.createEntityManager();
            EntityTransaction tx2 = em2.getTransaction();
            try {
                tx2.begin();
                Housing h2 = em2.find(Housing.class, housing.getId());
                em2.lock(h2, LockModeType.PESSIMISTIC_WRITE);

                Long overlap2 = em2.createQuery(
                                "SELECT COUNT(b) FROM Booking b WHERE b.housing.id = :hId " +
                                        "AND b.dateOfStart < :end AND b.dateOfEnd > :start", Long.class)
                        .setParameter("hId", housing.getId())
                        .setParameter("start", start)
                        .setParameter("end", end)
                        .getSingleResult();

                if (overlap2 > 0) {
                    tx2.rollback();
                    System.out.println("✅ Ожидаемая ошибка: жильё уже занято на этот период");
                }
            } finally {
                if (tx2.isActive()) tx2.rollback();
                em2.close();
            }

            // 🧹 Очистка: удаляем тестовую бронь
            tx.begin();
            em.remove(booking);
            tx.commit();
            System.out.printf("🧹 Тестовая бронь #%d удалена%n", booking.getId());

        } catch (IllegalStateException e) {
            System.out.printf("✅ Ожидаемое исключение: %s%n", e.getMessage());
        } catch (Exception e) {
            if (tx != null && tx.isActive()) tx.rollback();
            System.err.println("❌ Неожиданная ошибка: " + e.getMessage());
        } finally {
            if (em != null && em.isOpen()) em.close();
        }
        printDivider();
    }

    // ─────────────────────────────────────────────────────
    // Запуск всех демо
    // ─────────────────────────────────────────────────────
    public void runAll() {
        demoRead();
        demoCreate();
        demoUpdate();
        demoDelete();
        demoTransaction();
    }

    // ─────────────────────────────────────────────────────
    // Утилиты вывода
    // ─────────────────────────────────────────────────────
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
