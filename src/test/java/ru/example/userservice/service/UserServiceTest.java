package ru.example.userservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.example.userservice.dao.UserDao;
import ru.example.userservice.exception.UserServiceException;
import ru.example.userservice.model.User;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao; // Создаем изолированную заглушку для слоя DAO

    private UserService userService;

    @BeforeEach
    void setUp() {
        // Внедряем заглушку в тестируемый сервис перед каждым тестом
        userService = new UserServiceImpl(userDao);
    }

    @Test
    void createUser_Success_WhenAgeIsAdult() {
        User adultUser = new User("Иван", "ivan@mail.com", 25);

        userService.createUser(adultUser);

        // Проверяем, что сервис действительно вызвал метод сохранения в DAO 1 раз
        verify(userDao, times(1)).save(adultUser);
    }

    @Test
    void createUser_ThrowsException_WhenUserIsUnderage() {
        User underageUser = new User("Миша", "misha@mail.com", 16);

        // Проверяем выброс бизнес-исключения для несовершеннолетних
        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
            userService.createUser(underageUser);
        });

        assertEquals("Регистрация доступна только с 18 лет.", exception.getMessage());
        // Убеждаемся, что метод сохранения базы данных НЕ вызывался
        verify(userDao, never()).save(any());
    }

    @Test
    void getUserById_Success_WhenIdIsValid() {
        User expectedUser = new User("Анна", "anna@mail.com", 20);
        expectedUser.setId(1L);

        // Обучаем заглушку возвращать Анну при передаче ID = 1
        when(userDao.findById(1L)).thenReturn(expectedUser);

        User actualUser = userService.getUserById(1L);

        assertNotNull(actualUser);
        assertEquals("Анна", actualUser.getName());
        verify(userDao, times(1)).findById(1L);
    }

    @Test
    void getUserById_ThrowsException_WhenIdIsNegative() {
        UserServiceException exception = assertThrows(UserServiceException.class, () -> {
            userService.getUserById(-5L);
        });

        assertEquals("Некорректный ID пользователя.", exception.getMessage());
        verify(userDao, never()).findById(anyLong());
    }
}
