package ru.example.userservice.service;

import ru.example.userservice.dao.UserDao;
import ru.example.userservice.exception.UserServiceException;
import ru.example.userservice.model.User;
import java.util.List;

public class UserServiceImpl implements UserService {
    private final UserDao userDao;

    // Конструктор необходим для внедрения зависимости (Dependency Injection)
    public UserServiceImpl(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public void createUser(User user) {
        if (user.getAge() < 18) {
            throw new UserServiceException("Регистрация доступна только с 18 лет.", null);
        }
        userDao.save(user);
    }

    @Override
    public User getUserById(Long id) {
        if (id == null || id <= 0) {
            throw new UserServiceException("Некорректный ID пользователя.", null);
        }
        return userDao.findById(id);
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    @Override
    public void updateUser(User user) {
        userDao.update(user);
    }

    @Override
    public void deleteUser(Long id) {
        userDao.delete(id);
    }
}
