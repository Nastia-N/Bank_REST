package com.example.bankcards.util;

public class ValidationUtils {

    /**
     * Алгоритм Луна для проверки номера карты
     */
    public static boolean isValidCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() != 16) {
            return false;
        }

        int sum = 0;
        boolean alternate = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cardNumber.substring(i, i + 1));

            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }

            sum += n;
            alternate = !alternate;
        }

        return (sum % 10 == 0);
    }

    /**
     * Проверка срока действия карты
     */
    public static boolean isExpired(java.time.LocalDate expirationDate) {
        return expirationDate.isBefore(java.time.LocalDate.now());
    }
}
