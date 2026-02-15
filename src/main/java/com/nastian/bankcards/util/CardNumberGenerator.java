package com.nastian.bankcards.util;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;
import java.util.Random;

/**
 * Генератор номеров банковских карт.
 * <p>
 * Создает 16-значные номера карт с использованием SecureRandom
 * для обеспечения криптографической стойкости.
 */
@Component
public class CardNumberGenerator {

    private static final Random random = new SecureRandom();
    private static final int CARD_NUMBER_LENGTH = 16;

    /**
     * Генерирует 16-значный номер карты.
     *
     * @return строка из 16 цифр
     */
    public String generate() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CARD_NUMBER_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * Генерирует номер карты с заданным префиксом.
     * <p>
     * Используется для тестирования или специальных серий карт.
     *
     * @param prefix начальные цифры номера
     * @return номер карты, начинающийся с prefix
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