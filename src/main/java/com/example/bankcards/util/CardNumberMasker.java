package com.example.bankcards.util;

import org.springframework.stereotype.Component;

@Component
public class CardNumberMasker {

    /**
     * Маскирует номер карты: **** **** **** 1234
     */
    public String mask(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }

        String lastFour = cardNumber.substring(cardNumber.length() - 4);
        return "**** **** **** " + lastFour;
    }

    /**
     * Маскирует с кастомным разделителем
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
