package com.example.bankcards.util;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;
import java.util.Random;

@Component
public class CardNumberGenerator {

    private static final Random random = new SecureRandom();
    private static final int CARD_NUMBER_LENGTH = 16;

    /**
     * Генерирует 16-значный номер карты
     */
    public String generate() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CARD_NUMBER_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Генерирует номер с префиксом (например, 4 для Visa)
     */
    public String generateWithPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return generate();
        }

        StringBuilder sb = new StringBuilder(prefix);
        for (int i = prefix.length(); i < CARD_NUMBER_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }
}
