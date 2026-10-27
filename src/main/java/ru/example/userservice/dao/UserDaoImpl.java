package ru.example.userservice.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.example.userservice.exception.UserServiceException;
import ru.example.userservice.model.User;
import ru.example.userservice.util.HibernateUtil;

import java.util.List;

public class UserDaoImpl implements UserDao {
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImpl.class);

    @Override
    public void save(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
            logger.info("Пользователь успешно сохранен: {}", user.getEmail());
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Ошибка при сохранении пользователя с email: {}", user.getEmail(), e);
            throw new UserServiceException("Не удалось сохранить пользователя. Возможно, такой email уже зарегистрирован.", e);
        }
    }

    @Override
    public User findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, id);
        } catch (Exception e) {
            logger.error("Ошибка БД при поиске пользователя по ID: {}", id, e);
            throw new UserServiceException("Ошибка при обращении к базе данных. Попробуйте позже.", e);
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from User", User.class).list();
        } catch (Exception e) {
            logger.error("Ошибка БД при получении списка пользователей", e);
            throw new UserServiceException("Не удалось загрузить список пользователей из-за сбоя БД.", e);
        }
    }

    @Override
    public void update(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(user);
            transaction.commit();
            logger.info("Данные пользователя обновлены, ID: {}", user.getId());
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Ошибка БД при обновлении пользователя с ID: {}", user.getId(), e);
            throw new UserServiceException("Не удалось обновить данные. Проверьте уникальность email и подключение к БД.", e);
        }
    }

    @Override
    public void delete(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
                logger.info("Пользователь с ID {} удален", id);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Ошибка БД при удалении пользователя с ID: {}", id, e);
            throw new UserServiceException("Не удалось удалить пользователя из-за внутренней ошибки БД.", e);
        }
    }
}
