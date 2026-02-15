package com.nastian.bankcards.exception;

public class UnauthorizedAccessException extends RuntimeException {
    public UnauthorizedAccessException(String message) {
        super(message);
    }

    public UnauthorizedAccessException(Long userId, String resource) {
        super("User " + userId + " does not have access to " + resource);
    }
}
