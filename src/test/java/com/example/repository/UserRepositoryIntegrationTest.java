package com.example.repository;

import com.example.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
class UserRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void setupDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void cleanup() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS users");
        jdbcTemplate.execute("CREATE TABLE users (" +
                "id BIGSERIAL PRIMARY KEY, " +
                "username VARCHAR(100) UNIQUE NOT NULL, " +
                "email VARCHAR(200) NOT NULL)");
    }

    @Test
    void testInsertUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        User result = userRepository.insert(user);

        assertNotNull(result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void testSelectById() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        User created = userRepository.insert(user);

        Optional<User> found = userRepository.selectById(created.getId());

        assertTrue(found.isPresent());
        assertEquals(created.getId(), found.get().getId());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void testSelectByIdNotFound() {
        Optional<User> found = userRepository.selectById(999L);
        assertFalse(found.isPresent());
    }

    @Test
    void testSelectByUsername() {
        User user = new User();
        user.setUsername("johndoe");
        user.setEmail("john@example.com");
        userRepository.insert(user);

        Optional<User> found = userRepository.selectByUsername("johndoe");

        assertTrue(found.isPresent());
        assertEquals("johndoe", found.get().getUsername());
    }

    @Test
    void testSelectAll() {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        userRepository.insert(user1);

        User user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        userRepository.insert(user2);

        List<User> users = userRepository.selectAll();

        assertEquals(2, users.size());
    }

    @Test
    void testDelete() {
        User user = new User();
        user.setUsername("deleteuser");
        user.setEmail("delete@example.com");
        User created = userRepository.insert(user);

        boolean deleted = userRepository.delete(created.getId());

        assertTrue(deleted);
        assertFalse(userRepository.selectById(created.getId()).isPresent());
    }

    @Test
    void testUpdate() {
        User user = new User();
        user.setUsername("original");
        user.setEmail("original@example.com");
        User created = userRepository.insert(user);

        created.setUsername("updated");
        created.setEmail("updated@example.com");
        User updated = userRepository.update(created);

        assertEquals("updated", updated.getUsername());
        assertEquals("updated@example.com", updated.getEmail());
    }

    @Test
    void testUniqueUsername() {
        User user1 = new User();
        user1.setUsername("sameuser");
        user1.setEmail("email1@example.com");
        userRepository.insert(user1);

        User user2 = new User();
        user2.setUsername("sameuser");
        user2.setEmail("email2@example.com");

        assertThrows(Exception.class, () -> userRepository.insert(user2));
    }
}