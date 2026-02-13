package com.example.bankcards.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(Long userId) {
        super("User not found with id: " + userId);
    }

    public UserNotFoundException(String username, String type) {
        super("User not found with " + type + ": " + username);
    }
}
