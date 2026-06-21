package com.cinema.util;

import com.cinema.entity.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
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

            // Проверка на дубликаты
            Long guestsCount = em.createQuery("SELECT COUNT(g) FROM Guest g", Long.class)
                    .getSingleResult();
            if (guestsCount > 0) {
                tx.commit();
                log.info("Начальные данные уже есть, заполнение пропущено");
                return;
            }

            // 1. Справочники: типы жилья и условия
            TypeOfHousing tent = new TypeOfHousing("Палатка");
            TypeOfHousing cabin = new TypeOfHousing("Домик");
            TypeOfHousing suite = new TypeOfHousing("Люкс-коттедж");
            TypeOfHousing glamping = new TypeOfHousing("Глэмпинг");
            List.of(tent, cabin, suite, glamping).forEach(em::persist);

            HousingCondition basic = new HousingCondition("Базовые удобства");
            HousingCondition comfort = new HousingCondition("Комфорт");
            HousingCondition premium = new HousingCondition("Премиум");
            HousingCondition luxury = new HousingCondition("Люкс");
            List.of(basic, comfort, premium, luxury).forEach(em::persist);
            em.flush();

            // 2. Кемпы
            Camp forestCamp = new Camp("Лесной кемп «Тишина»");
            Camp lakeCamp = new Camp("Озёрный кемп «Рассвет»");
            Camp mountainCamp = new Camp("Горный кемп «Вершина»");
            List.of(forestCamp, lakeCamp, mountainCamp).forEach(em::persist);
            em.flush();

            // 3. Инвентарь для каждого кемпа
            persistInventory(em, forestCamp, List.of(
                    new String[]{"Мангал", "10"},
                    new String[]{"Столы пикниковые", "5"},
                    new String[]{"Скамейки", "10"},
                    new String[]{"Урны", "8"},
                    new String[]{"Фонари", "15"}
            ));
            persistInventory(em, lakeCamp, List.of(
                    new String[]{"Лодки", "4"},
                    new String[]{"Спасательные жилеты", "12"},
                    new String[]{"Рыболовные снасти", "6"},
                    new String[]{"Палатки для рыбаков", "8"}
            ));
            persistInventory(em, mountainCamp, List.of(
                    new String[]{"Трекинговые палки", "20"},
                    new String[]{"Рюкзаки", "10"},
                    new String[]{"Термосы", "15"},
                    new String[]{"Аптечки", "5"}
            ));

            // 4. Жильё
            Housing h1 = new Housing(tent, forestCamp, "Стандарт", 2, new BigDecimal("1500.00"), basic);
            Housing h2 = new Housing(tent, forestCamp, "Улучшенная", 3, new BigDecimal("2200.00"), comfort);
            Housing h3 = new Housing(cabin, lakeCamp, "У озера", 4, new BigDecimal("4500.00"), comfort);
            Housing h4 = new Housing(cabin, lakeCamp, "С видом", 4, new BigDecimal("5200.00"), premium);
            Housing h5 = new Housing(suite, mountainCamp, "Панорамный", 6, new BigDecimal("8900.00"), premium);
            Housing h6 = new Housing(glamping, mountainCamp, "Премиум-глэмп", 2, new BigDecimal("12000.00"), luxury);
            Housing h7 = new Housing(tent, mountainCamp, "Треккинг-старт", 2, new BigDecimal("1800.00"), basic);
            Housing h8 = new Housing(cabin, forestCamp, "Семейный", 5, new BigDecimal("3800.00"), comfort);

            List.of(h1, h2, h3, h4, h5, h6, h7, h8).forEach(em::persist);
            em.flush();

            // 5. Дополнительные услуги
            AdditionalServices breakfast = new AdditionalServices("Завтрак в номер", new BigDecimal("500.00"));
            AdditionalServices transfer = new AdditionalServices("Трансфер от вокзала", new BigDecimal("1200.00"));
            AdditionalServices equipRent = new AdditionalServices("Аренда снаряжения", new BigDecimal("800.00"));
            AdditionalServices guide = new AdditionalServices("Услуги гида (день)", new BigDecimal("2500.00"));
            AdditionalServices sauna = new AdditionalServices("Сауна (2 часа)", new BigDecimal("1500.00"));
            AdditionalServices bbq = new AdditionalServices("Набор для BBQ", new BigDecimal("600.00"));
            List.of(breakfast, transfer, equipRent, guide, sauna, bbq).forEach(em::persist);


            // 6. Гости
            Guest guest1 = new Guest("Петров", "Иван", "Сергеевич", "4501234567");
            Guest guest2 = new Guest("Смирнова", "Анна", "Дмитриевна", "4507654321");
            Guest guest3 = new Guest( "Олег", "Иванов", "4501112233"); // отчество опционально
            Guest guest4 = new Guest( "Кузнецова","Мария", "Алексеевна", "4504445566");
            Guest guest5 = new Guest("Соколов","Дмитрий",  "Павлович", "4507778899");
            Guest guest6 = new Guest( "Елена", "Волкова", "4507778819");
            List.of(guest1, guest2, guest3, guest4, guest5, guest6).forEach(em::persist);
            em.flush();

            // 7. Бронирования
            OffsetDateTime baseDate = OffsetDateTime.of(2026, 7, 1, 14, 0, 0, 0, ZoneOffset.ofHours(3));

            // Бронь 1: палатка в лесу, 3 дня
            Booking b1 = new Booking(guest1, h1, breakfast,
                    baseDate, baseDate.plusDays(3), null);

            // Бронь 2: домик у озера, 5 дней, с трансфером
            Booking b2 = new Booking(guest2, h3, transfer,
                    baseDate.plusDays(2), baseDate.plusDays(7), null);

            // Бронь 3: люкс в горах, 2 дня, с гидом и сауной (доп.услуга одна, но можно расширить)
            Booking b3 = new Booking(guest3, h5, guide,
                    baseDate.plusDays(5), baseDate.plusDays(7), baseDate.plusDays(5).plusHours(2));

            // Бронь 4: глэмпинг, 4 дня, премиум-пакет
            Booking b4 = new Booking(guest4, h6, sauna,
                    baseDate.plusDays(10), baseDate.plusDays(14), null);

            // Бронь 5: семейный домик, 7 дней, с завтраками
            Booking b5 = new Booking(guest5, h8, breakfast,
                    baseDate.plusDays(15), baseDate.plusDays(22), null);

            // Бронь 6: короткая бронь, без доп.услуг
            Booking b6 = new Booking(guest6, h7, null,
                    baseDate.plusDays(20), baseDate.plusDays(21), null);

            List.of(b1, b2, b3, b4, b5, b6).forEach(em::persist);

            tx.commit();
            log.info("Начальные данные для кемпинг-системы добавлены: {} гостей, {} объектов жилья, {} броней",
                    guestsCount, 8, 6);

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
                log.error("Ошибка при заполнении данных, выполнен откат", e);
            }
            throw e;
        } finally {
            em.close();
        }
    }

    /**
     * Вспомогательный метод для массового добавления инвентаря в кемп
     */
    private static void persistInventory(EntityManager em, Camp camp, List<String[]> items) {
        for (String[] item : items) {
            String type = item[0];
            int quantity = Integer.parseInt(item[1]);
            Inventory inv = new Inventory(camp, type, quantity);
            em.persist(inv);
        }
    }

    /**
     * Метод для очистки всех данных
     */
    public static void clearAll() {
        EntityManager em = HibernateUtil.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("DELETE FROM Booking").executeUpdate();
            em.createQuery("DELETE FROM Inventory").executeUpdate();
            em.createQuery("DELETE FROM Housing").executeUpdate();
            em.createQuery("DELETE FROM AdditionalServices").executeUpdate();
            em.createQuery("DELETE FROM Guest").executeUpdate();
            em.createQuery("DELETE FROM HousingCondition").executeUpdate();
            em.createQuery("DELETE FROM TypeOfHousing").executeUpdate();
            em.createQuery("DELETE FROM Camp").executeUpdate();
            tx.commit();
            log.info("Все данные очищены");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            log.error("Ошибка при очистке данных", e);
            throw e;
        } finally {
            em.close();
        }
    }
}
