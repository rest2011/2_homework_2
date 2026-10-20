package ru.example.userservice.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.example.userservice.model.User;
import ru.example.userservice.util.HibernateUtil;

import java.util.Collections;
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
        } catch (ConstraintViolationException e) {
            if (transaction != null) transaction.rollback();
            logger.error("Ошибка уникальности: Пользователь с email {} уже существует", user.getEmail());
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Ошибка при сохранении пользователя", e);
        }
    }

    @Override
    public User findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(User.class, id);
        } catch (Exception e) {
            logger.error("Ошибка при поиске пользователя по ID: {}", id, e);
            return null;
        }
    }

    @Override
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("from User", User.class).list();
        } catch (Exception e) {
            logger.error("Ошибка при получении списка пользователей", e);
            return Collections.emptyList();
        }
    }

    @Override
    public void update(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.merge(user);
            transaction.commit();
            logger.info("Данные пользователя обновлены: {}", user.getId());
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Ошибка при обновлении пользователя с ID: {}", user.getId(), e);
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
            } else {
                logger.warn("Пользователь с ID {} не найден для удаления", id);
            }
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) transaction.rollback();
            logger.error("Ошибка при удалении пользователя с ID: {}", id, e);
        }
    }
}
