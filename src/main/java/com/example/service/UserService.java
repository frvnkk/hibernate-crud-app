package com.example.service;

import com.example.dao.UserDao;
import com.example.dao.UserDaoImpl;
import com.example.entity.User;
import lombok.extern.log4j.Log4j2;

import java.util.List;
import java.util.Optional;

@Log4j2
public class UserService {

    private final UserDao userDao;

    public UserService() {
        this.userDao = new UserDaoImpl();
    }

    public User createUser(String name, String email, Integer age) {
        log.info("Создание пользователя: {}, {}", name, email);

        Optional<User> existingUser = userDao.findByEmail(email);
        if (existingUser.isPresent()) {
            log.warn("Пользователь с email {} уже существует", email);
            throw new RuntimeException("Пользователь с таким email уже существует");
        }

        User user = User.builder()
                .name(name)
                .email(email)
                .age(age)
                .build();

        return userDao.save(user);
    }

    public Optional<User> getUserById(Long id) {
        log.info("Получение пользователя по ID: {}", id);
        return userDao.findById(id);
    }

    public List<User> getAllUsers() {
        log.info("Получение всех пользователей");
        return userDao.findAll();
    }

    public User updateUser(Long id, String name, String email, Integer age) {
        log.info("Обновление пользователя ID: {}", id);

        Optional<User> optionalUser = userDao.findById(id);
        if (optionalUser.isEmpty()) {
            log.warn("Пользователь с ID {} не найден", id);
            throw new RuntimeException("Пользователь не найден");
        }

        User user = optionalUser.get();
        user.setName(name);
        user.setEmail(email);
        user.setAge(age);

        return userDao.update(user);
    }

    public void deleteUser(Long id) {
        log.info("Удаление пользователя ID: {}", id);
        userDao.delete(id);
    }
}