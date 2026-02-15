package com.nastian.bankcards.util;

import org.springframework.stereotype.Component;

/**
 * Маскировщик номеров карт для безопасного отображения.
 * <p>
 * Преобразует полный номер карты в формат с маской, например:
 * "1234567890123456" -> "**** **** **** 3456"
 */
@Component
public class CardNumberMasker {

    /**
     * Маскирует номер карты в стандартном формате: **** **** **** 1234.
     *
     * @param cardNumber полный номер карты
     * @return замаскированный номер
     */
    public String mask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }

        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }

    /**
     * Маскирует номер карты с возможностью кастомизации.
     *
     * @param cardNumber полный номер карты
     * @param maskChar символ для маски (обычно '*')
     * @param separator разделитель групп (обычно ' ')
     * @return замаскированный номер в кастомном формате
     */
    public String mask(String cardNumber, String maskChar, String separator) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return maskChar.repeat(4);
        }

        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return maskChar.repeat(4) + separator +
                maskChar.repeat(4) + separator +
                maskChar.repeat(4) + separator +
                lastFour;
    }
}