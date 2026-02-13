package com.example.bankcards.exception;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public class InsufficientFundsException extends RuntimeException {

  public InsufficientFundsException(String message) {
    super(message);
  }

  public InsufficientFundsException(Long cardId, BigDecimal available, BigDecimal requested) {
    super(String.format(
            "Insufficient funds on card %d. Available: %s, Requested: %s, Missing: %s",
            cardId,
            formatCurrency(available),
            formatCurrency(requested),
            formatCurrency(requested.subtract(available))
    ));
  }

  private static String formatCurrency(BigDecimal amount) {
    return NumberFormat.getCurrencyInstance(Locale.US).format(amount);
  }
}