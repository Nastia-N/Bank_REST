package com.example.bankcards.service;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.entity.User;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;

    public AuthService(UserService userService) {
        this.userService = userService;
    }

    public User register(AuthRequest request) {
        return userService.register(request);
    }

    public User findByUsername(String username) {
        return userService.findByUsername(username);
    }
}