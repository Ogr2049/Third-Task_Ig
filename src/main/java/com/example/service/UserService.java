package com.example.service;

import com.example.entity.User;
import com.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    public User addUser(String username, String email) {
        if (userRepository.selectByUsername(username).isPresent()) {
            throw new RuntimeException("Username already exists: " + username);
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        return userRepository.insert(user);
    }
    
    public User getUserById(Long id) {
        return userRepository.selectById(id)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }
    
    public User getUserByUsername(String username) {
        return userRepository.selectByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
    }
    
    public List<User> getAllUsers() {
        return userRepository.selectAll();
    }
    
    public void removeUser(Long id) {
        if (!userRepository.delete(id)) {
            throw new RuntimeException("User not found for deletion: " + id);
        }
    }
    
    public User changeEmail(Long id, String newEmail) {
        User user = getUserById(id);
        user.setEmail(newEmail);
        return userRepository.update(user);
    }
}