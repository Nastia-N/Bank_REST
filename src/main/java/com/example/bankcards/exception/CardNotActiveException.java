package com.example.bankcards.exception;

public class CardNotActiveException extends RuntimeException {
    public CardNotActiveException(String message) {
        super(message);
    }

    public CardNotActiveException(Long cardId, String status) {
        super("Card " + cardId + " is not active. Current status: " + status);
    }
}
