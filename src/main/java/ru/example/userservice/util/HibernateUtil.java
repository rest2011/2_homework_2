package ru.example.userservice.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateUtil {
    private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
    private static SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Пытаемся прочитать стандартный hibernate.cfg.xml
            return new Configuration().configure().buildSessionFactory();
        } catch (Throwable ex) {
            // ВАЖНО ДЛЯ ТЕСТОВ: если локальный пароль не найден или база отключена,
            // мы не роняем все приложение сразу, а просто пишем предупреждение.
            // Интеграционный тест сам заменит эту фабрику на Testcontainers в блоке @BeforeAll!
            logger.warn("Локальный SessionFactory не инициализирован (это нормально для интеграционных тестов): " + ex.getMessage());
            return null;
        }
    }

    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            throw new IllegalStateException("SessionFactory не была создана. Проверьте настройки подключения к БД.");
        }
        return sessionFactory;
    }

    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            sessionFactory.close();
            sessionFactory = null; // Обнуляем ссылку для возможности перезапуска в тестах
        }
    }
}
