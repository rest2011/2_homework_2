package ru.example.userservice.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users") // Имя таблицы в БД
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Автоинкремент для PostgreSQL
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private Integer age;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Hibernate требует наличие пустого конструктора
    public User() {}

    // Конструктор для удобства создания
    public User(String name, String email, Integer age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }

    // Метод выполнится автоматически перед сохранением в БД
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Геттеры и Сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "User{id=" + id + ", name='" + name + '\'' + ", email='" + email + '\'' + ", age=" + age + ", createdAt=" + createdAt + '}';
    }
}
