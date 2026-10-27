package ru.example.userservice;

import ru.example.userservice.dao.UserDao;
import ru.example.userservice.dao.UserDaoImpl;
import ru.example.userservice.exception.UserServiceException;
import ru.example.userservice.model.User;
import ru.example.userservice.util.HibernateUtil;

import java.util.List;
import java.util.Scanner;

public class Main {
    private static final UserDao userDao = new UserDaoImpl();
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("--- Приложение User Service запущено ---");
        try {
            boolean running = true;
            while (running) {
                printMenu();
                String choice = scanner.nextLine().trim();
                switch (choice) {
                    case "1" -> createUser();
                    case "2" -> readUser();
                    case "3" -> readAllUsers();
                    case "4" -> updateUser();
                    case "5" -> deleteUser();
                    case "0" -> {
                        running = false;
                        System.out.println("Выход из программы.");
                    }
                    default -> System.out.println("❌ Неверный ввод, выберите пункт от 0 до 5.");
                }
            }
        } finally {
            HibernateUtil.shutdown();
        }
    }

    private static void printMenu() {
        System.out.println("\nВыберите операцию:");
        System.out.println("1. Создать пользователя");
        System.out.println("2. Найти пользователя по ID");
        System.out.println("3. Показать всех пользователей");
        System.out.println("4. Обновить пользователя");
        System.out.println("5. Удалить пользователя");
        System.out.println("0. Выход");
        System.out.print("Введите цифру: ");
    }

    private static int readIntSafe() {
        while (true) {
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("❌ Ошибка ввода! Пожалуйста, введите целое число: ");
            }
        }
    }

    private static Long readLongSafe() {
        while (true) {
            try {
                return Long.parseLong(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.print("❌ Ошибка ввода! Пожалуйста, введите корректный ID (число): ");
            }
        }
    }

    private static boolean validateFields(String name, String email, int age) {
        if (name == null || name.isBlank() || name.length() < 2) {
            System.out.println("❌ Ошибка: Имя не должно быть пустым и должно содержать минимум 2 символа.");
            return false;
        }
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            System.out.println("❌ Ошибка: Введен некорректный формат Email.");
            return false;
        }
        if (age < 0 || age > 120) {
            System.out.println("❌ Ошибка: Возраст должен быть в диапазоне от 0 до 120.");
            return false;
        }
        return true;
    }

    private static void createUser() {
        System.out.print("Введите имя: ");
        String name = scanner.nextLine().trim();
        System.out.print("Введите email: ");
        String email = scanner.nextLine().trim();
        System.out.print("Введите возраст: ");
        int age = readIntSafe();

        if (!validateFields(name, email, age)) return;

        try {
            User user = new User(name, email, age);
            userDao.save(user);
            System.out.println("✅ Успех: Пользователь успешно создан!");
        } catch (UserServiceException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private static void readUser() {
        System.out.print("Введите ID пользователя: ");
        Long id = readLongSafe();

        try {
            User user = userDao.findById(id);
            if (user != null) {
                System.out.println("👤 Результат поиска: " + user);
            } else {
                System.out.println("ℹ️ Информация: Пользователь с ID " + id + " не найден в системе.");
            }
        } catch (UserServiceException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private static void readAllUsers() {
        try {
            List<User> users = userDao.findAll();
            if (users.isEmpty()) {
                System.out.println("ℹ️ Информация: База данных пользователей пуста.");
            } else {
                System.out.println("📋 Список всех пользователей:");
                users.forEach(System.out::println);
            }
        } catch (UserServiceException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private static void updateUser() {
        System.out.print("Введите ID пользователя для обновления: ");
        Long id = readLongSafe();

        try {
            User user = userDao.findById(id);
            if (user == null) {
                System.out.println("❌ Ошибка: Пользователь с ID " + id + " не найден.");
                return;
            }

            System.out.print("Введите новое имя (оставьте пустым, чтобы не менять): ");
            String name = scanner.nextLine().trim();
            if (!name.isBlank()) user.setName(name);

            System.out.print("Введите новый email (оставьте пустым, чтобы не менять): ");
            String email = scanner.nextLine().trim();
            if (!email.isBlank()) user.setEmail(email);

            System.out.print("Введите новый возраст (или -1, чтобы не менять): ");
            int age = readIntSafe();
            if (age != -1) user.setAge(age);

            // Проверяем данные перед отправкой в БД
            if (!validateFields(user.getName(), user.getEmail(), user.getAge())) return;

            userDao.update(user);
            System.out.println("✅ Успех: Данные пользователя успешно обновлены!");
        } catch (UserServiceException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }

    private static void deleteUser() {
        System.out.print("Введите ID пользователя для удаления: ");
        Long id = readLongSafe();

        try {
            User user = userDao.findById(id);
            if (user == null) {
                System.out.println("❌ Ошибка: Пользователь с ID " + id + " не существует.");
                return;
            }

            userDao.delete(id);
            System.out.println("✅ Успех: Пользователь с ID " + id + " успешно удален.");
        } catch (UserServiceException e) {
            System.out.println("❌ Ошибка: " + e.getMessage());
        }
    }
}
