package com.example.repository;

import com.example.entity.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepository {
    private final JdbcTemplate jdbcTemplate;
    
    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    @Override
    public User insert(User user) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO users (username, email) VALUES (?, ?)",
                PreparedStatement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getEmail());
            return ps;
        }, keyHolder);
        
        user.setId(keyHolder.getKey().longValue());
        return user;
    }
    
    @Override
    public Optional<User> selectById(Long id) {
        try {
            User user = jdbcTemplate.queryForObject(
                "SELECT id, username, email FROM users WHERE id = ?",
                (rs, rowNum) -> new User(
                    rs.getLong("id"),
                    rs.getString("username"),
                    rs.getString("email")
                ), id);
            return Optional.ofNullable(user);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public Optional<User> selectByUsername(String username) {
        try {
            User user = jdbcTemplate.queryForObject(
                "SELECT id, username, email FROM users WHERE username = ?",
                (rs, rowNum) -> new User(
                    rs.getLong("id"),
                    rs.getString("username"),
                    rs.getString("email")
                ), username);
            return Optional.ofNullable(user);
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    
    @Override
    public List<User> selectAll() {
        return jdbcTemplate.query(
            "SELECT id, username, email FROM users ORDER BY id",
            (rs, rowNum) -> new User(
                rs.getLong("id"),
                rs.getString("username"),
                rs.getString("email")
            ));
    }
    
    @Override
    public boolean delete(Long id) {
        int affected = jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
        return affected > 0;
    }
    
    @Override
    public User update(User user) {
        jdbcTemplate.update(
            "UPDATE users SET username = ?, email = ? WHERE id = ?",
            user.getUsername(), user.getEmail(), user.getId());
        return user;
    }
}