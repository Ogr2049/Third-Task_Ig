package com.example.repository;

import com.example.entity.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    User insert(User user);
    Optional<User> selectById(Long id);
    Optional<User> selectByUsername(String username);
    List<User> selectAll();
    boolean delete(Long id);
    User update(User user);
}