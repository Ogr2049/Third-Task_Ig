package com.example.service;

import com.example.entity.User;
import com.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testAddUser() {
        User user = new User(1L, "newuser", "new@example.com");
        when(userRepository.selectByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.insert(any(User.class))).thenReturn(user);

        User result = userService.addUser("newuser", "new@example.com");

        assertEquals(1L, result.getId());
        assertEquals("newuser", result.getUsername());
        verify(userRepository).selectByUsername("newuser");
        verify(userRepository).insert(any(User.class));
    }

    @Test
    void testAddUserDuplicate() {
        User existing = new User(1L, "existing", "existing@example.com");
        when(userRepository.selectByUsername("existing")).thenReturn(Optional.of(existing));

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.addUser("existing", "new@example.com"));

        assertEquals("Username already exists: existing", exception.getMessage());
        verify(userRepository, never()).insert(any(User.class));
    }

    @Test
    void testGetUserById() {
        User user = new User(1L, "testuser", "test@example.com");
        when(userRepository.selectById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testGetUserByIdNotFound() {
        when(userRepository.selectById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.getUserById(1L));

        assertEquals("User not found with id: 1", exception.getMessage());
    }

    @Test
    void testGetUserByUsername() {
        User user = new User(1L, "testuser", "test@example.com");
        when(userRepository.selectByUsername("testuser")).thenReturn(Optional.of(user));

        User result = userService.getUserByUsername("testuser");

        assertEquals("testuser", result.getUsername());
    }

    @Test
    void testGetAllUsers() {
        List<User> users = Arrays.asList(
            new User(1L, "user1", "user1@example.com"),
            new User(2L, "user2", "user2@example.com")
        );
        when(userRepository.selectAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userRepository).selectAll();
    }

    @Test
    void testRemoveUser() {
        when(userRepository.delete(1L)).thenReturn(true);

        userService.removeUser(1L);

        verify(userRepository).delete(1L);
    }

    @Test
    void testRemoveUserNotFound() {
        when(userRepository.delete(1L)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userService.removeUser(1L));

        assertEquals("User not found for deletion: 1", exception.getMessage());
    }

    @Test
    void testChangeEmail() {
        User user = new User(1L, "testuser", "old@example.com");
        when(userRepository.selectById(1L)).thenReturn(Optional.of(user));
        when(userRepository.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.changeEmail(1L, "new@example.com");

        assertEquals("new@example.com", result.getEmail());
        assertEquals("testuser", result.getUsername());
    }
}