package ru.example.userservice.dao;

import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.example.userservice.model.User;
import ru.example.userservice.util.HibernateUtil;

import java.lang.reflect.Field;
import java.util.List;

// Добавили правильный статический импорт для чистых ассертов
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class UserDaoIntegrationTest {

    // Принудительно настраиваем свойства окружения для обхода проблем с Docker Desktop на Windows
    static {
        System.setProperty("DOCKER_HOST", "tcp://127.0.0.1:2375");
        System.setProperty("TESTCONTAINERS_RYUK_DISABLED", "true");
    }

    // Автоматический запуск временной базы данных PostgreSQL в Docker
    @Container
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_password");

    private UserDao userDao;

    @BeforeAll
    static void beforeAll() throws Exception {
        // Динамически перенастраиваем HibernateUtil на работу с временным контейнером вместо локальной БД
        Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.connection.driver_class", postgresContainer.getDriverClassName());
        configuration.setProperty("hibernate.connection.url", postgresContainer.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", postgresContainer.getUsername());
        configuration.setProperty("hibernate.connection.password", postgresContainer.getPassword());
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");
        configuration.addAnnotatedClass(User.class);

        // Используем рефлексию Java, чтобы подменить финальное поле фабрики в утилите
        Field factoryField = HibernateUtil.class.getDeclaredField("sessionFactory");
        factoryField.setAccessible(true);

        HibernateUtil.shutdown();
        factoryField.set(null, configuration.buildSessionFactory());
    }

    @AfterAll
    static void afterAll() {
        HibernateUtil.shutdown();
    }

    @BeforeEach
    void setUp() {
        userDao = new UserDaoImpl();
        // Обеспечиваем изоляцию тестов: полностью очищаем таблицу перед каждым тестом
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            session.beginTransaction();
            session.createMutationQuery("delete from User").executeUpdate();
            session.getTransaction().commit();
        }
    }

    @Test
    void saveAndFindById_Success() {
        User user = new User("Тест Контейнеров", "container@test.com", 30);

        userDao.save(user);

        assertNotNull(user.getId(), "База данных должна автоматически назначить ID");

        User savedUser = userDao.findById(user.getId());
        assertNotNull(savedUser);

        // Теперь методы вызываются коротко и без ошибок компиляции
        assertEquals("Тест Контейнеров", savedUser.getName());
        assertEquals("container@test.com", savedUser.getEmail());
    }

    @Test
    void findAll_ReturnsAllUsers() {
        User user1 = new User("Первый", "first@test.com", 20);
        User user2 = new User("Второй", "second@test.com", 22);

        userDao.save(user1);
        userDao.save(user2);

        List<User> list = userDao.findAll();

        assertEquals(2, list.size(), "Должно вернуться ровно 2 пользователя");
    }

    @Test
    void delete_RemovesUserFromDb() {
        User user = new User("Удаляемый", "delete@test.com", 40);
        userDao.save(user);

        userDao.delete(user.getId());

        User deletedUser = userDao.findById(user.getId());
        assertNull(deletedUser, "Пользователь должен отсутствовать в базе после удаления");
    }
}
