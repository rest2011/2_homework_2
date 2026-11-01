package ru.example.userservice.dao;

import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.example.userservice.model.User;
import ru.example.userservice.util.HibernateUtil;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// 1. МЫ УБРАЛИ аннотацию @Testcontainers сверху класса!
class UserDaoIntegrationTest {

    // 2. МЫ УБРАЛИ аннотацию @Container отсюда! Теперь управляем запуском вручную.
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_password");

    private UserDao userDao;

    @BeforeAll
    static void beforeAll() throws Exception {
        // ШАГ 1: Первым делом жестко прописываем порты Docker, пока ничего не запустилось
        System.setProperty("DOCKER_HOST", "tcp://127.0.0.1:2375");
        System.setProperty("TESTCONTAINERS_RYUK_DISABLED", "true");
        System.setProperty("api.version", "1.44");

        // ШАГ 2: Вручную запускаем контейнер. Теперь свойства DOCKER_HOST точно применятся!
        postgresContainer.start();

        // ШАГ 3: Настраиваем тестовый Hibernate на параметры запущенного контейнера
        Configuration configuration = new Configuration();
        configuration.setProperty("hibernate.connection.driver_class", postgresContainer.getDriverClassName());
        configuration.setProperty("hibernate.connection.url", postgresContainer.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", postgresContainer.getUsername());
        configuration.setProperty("hibernate.connection.password", postgresContainer.getPassword());
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");
        configuration.addAnnotatedClass(User.class);

        // ШАГ 4: Через рефлексию подменяем фабрику в HibernateUtil
        Field factoryField = HibernateUtil.class.getDeclaredField("sessionFactory");
        factoryField.setAccessible(true);

        HibernateUtil.shutdown(); // Сбрасываем старую (если была)
        factoryField.set(null, configuration.buildSessionFactory()); // Записываем чистую тестовую фабрику
    }

    @AfterAll
    static void afterAll() {
        HibernateUtil.shutdown();
        if (postgresContainer != null) {
            postgresContainer.stop(); // Останавливаем контейнер вручную после всех тестов
        }
    }

    @BeforeEach
    void setUp() {
        userDao = new UserDaoImpl();
        // Полностью очищаем таблицу перед каждым тестом для изоляции
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
