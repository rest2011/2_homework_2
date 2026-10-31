package ru.example.userservice.service;

import ru.example.userservice.model.User;
import java.util.List;

public interface UserService {
    void createUser(User user);
    User getUserById(Long id);
    List<User> getAllUsers();
    void updateUser(User user);
    void deleteUser(Long id);
}
