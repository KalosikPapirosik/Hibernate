package com.cinema;

import com.cinema.service.BusinessQueryService;
import com.cinema.service.CrudDemoService;
import com.cinema.service.CrudDemoService2;
import com.cinema.util.DataSeeder;
import com.cinema.util.HibernateUtil;

/**
 * Запуск: mvn compile exec:java
 */
public class Main {

    public static void main(String[] args) {
        try {
            // Инициализация EntityManagerFactory (аналог ConnectionManager.init())
            HibernateUtil.getEntityManagerFactory();
            DataSeeder.seed();
            System.out.println("Hibernate инициализирован, схема создана и заполнена\n");

            // CRUD-демонстрация
            System.out.println("CRUD-операции через Hibernate");

            CrudDemoService2 crudDemo = new CrudDemoService2();
            crudDemo.start();

            // Бизнес-запросы
            System.out.println("Примеры бизнес-запросов (JPQL)");

            BusinessQueryService queryService = new BusinessQueryService();
            queryService.runAll();

        } catch (Exception e) {
            System.err.println("Ошибка: " + e.getMessage());
            e.printStackTrace();
        } finally {
            HibernateUtil.close();
            System.out.println("\nHibernate закрыт. Готово.");
        }
    }
}
