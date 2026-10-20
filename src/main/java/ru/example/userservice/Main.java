package ru.example.userservice;

import ru.example.userservice.dao.UserDao;
import ru.example.userservice.dao.UserDaoImpl;
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
                String choice = scanner.nextLine();
                switch (choice) {
                    case "1" -> createUser();
                    case "2" -> readUser();
                    case "3" -> readAllUsers();
                    case "4" -> updateUser();
                    case "5" -> deleteUser();
                    case "0" -> {
                        running = false;
                        HibernateUtil.shutdown();
                        System.out.println("Выход из программы.");
                    }
                    default -> System.out.println("Неверный ввод, попробуйте еще раз.");
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

    private static void createUser() {
        System.out.print("Введите имя: ");
        String name = scanner.nextLine();
        System.out.print("Введите email: ");
        String email = scanner.nextLine();
        System.out.print("Введите возраст: ");
        int age = Integer.parseInt(scanner.nextLine());

        User user = new User(name, email, age);
        userDao.save(user);
    }

    private static void readUser() {
        System.out.print("Введите ID пользователя: ");
        Long id = Long.parseLong(scanner.nextLine());
        User user = userDao.findById(id);
        if (user != null) {
            System.out.println("Найден: " + user);
        } else {
            System.out.println("Пользователь не найден.");
        }
    }

    private static void readAllUsers() {
        List<User> users = userDao.findAll();
        if (users.isEmpty()) {
            System.out.println("База данных пуста.");
        } else {
            users.forEach(System.out::println);
        }
    }

    private static void updateUser() {
        System.out.print("Введите ID пользователя для обновления: ");
        Long id = Long.parseLong(scanner.nextLine());
        User user = userDao.findById(id);
        if (user == null) {
            System.out.println("Пользователь не найден.");
            return;
        }

        System.out.print("Введите новое имя (оставьте пустым чтобы не менять): ");
        String name = scanner.nextLine();
        if (!name.isBlank()) user.setName(name);

        System.out.print("Введите новый email (оставьте пустым чтобы не менять): ");
        String email = scanner.nextLine();
        if (!email.isBlank()) user.setEmail(email);

        System.out.print("Введите новый возраст (или -1 чтобы не менять): ");
        int age = Integer.parseInt(scanner.nextLine());
        if (age != -1) user.setAge(age);

        userDao.update(user);
    }

    private static void deleteUser() {
        System.out.print("Введите ID пользователя для удаления: ");
        Long id = Long.parseLong(scanner.nextLine());
        userDao.delete(id);
    }
}
