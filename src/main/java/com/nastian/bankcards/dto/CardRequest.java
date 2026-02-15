package com.nastian.bankcards.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * DTO для запроса на создание карты.
 * <p>
 * Содержит данные, которые пользователь указывает при создании карты:
 * <ul>
 *   <li>Имя владельца (как на карте)</li>
 *   <li>Срок действия</li>
 * </ul>
 * Номер карты генерируется автоматически на сервере.
 */

@Data
public class CardRequest {

    @NotBlank(message = "Card holder name is required")
    @Size(min = 2, max = 100, message = "Card holder name must be between 2 and 100 characters")
    private String cardHolderName;

    @NotNull(message = "Expiration date is required")
    @Future(message = "Expiration date must be in the future")
    private LocalDate expirationDate;
}