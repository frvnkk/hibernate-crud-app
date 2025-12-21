package com.example.dao;

import com.example.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserDao {

    User save(User user);

    Optional<User> findById(Long id);

    List<User> findAll();

    User update(User user);

    void delete(Long id);

    Optional<User> findByEmail(String email);
}