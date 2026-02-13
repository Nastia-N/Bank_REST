package com.example.bankcards.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resource, String value) {
        super(String.format("%s already exists with value: %s", resource, value));
    }
}
