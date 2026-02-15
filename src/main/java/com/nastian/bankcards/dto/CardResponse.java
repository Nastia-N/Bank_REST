package com.nastian.bankcards.dto;

import com.nastian.bankcards.entity.Card;
import com.nastian.bankcards.entity.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO для ответа с данными карты.
 * <p>
 * Содержит полную информацию о карте для отображения клиенту:
 * <ul>
 *   <li>ID карты</li>
 *   <li>Маскированный номер (**** **** **** 1234)</li>
 *   <li>Имя владельца</li>
 *   <li>Срок действия</li>
 *   <li>Статус</li>
 *   <li>Баланс</li>
 *   <li>ID владельца</li>
 * </ul>
 */

@Data
@Schema(description = "Ответ с данными банковской карты")
public class CardResponse {

    @Schema(description = "ID карты", example = "1")
    private Long id;

    @Schema(description = "Маскированный номер карты", example = "**** **** **** 1234")
    private String cardNumberMasked;

    @Schema(description = "Имя владельца на карте", example = "IVAN PETROV")
    private String cardHolderName;

    @Schema(description = "Срок действия", example = "2025-12-31")
    private LocalDate expirationDate;

    @Schema(description = "Статус карты", example = "ACTIVE")
    private CardStatus status;

    @Schema(description = "Баланс", example = "15000.50")
    private BigDecimal balance;

    @Schema(description = "ID владельца карты", example = "1")
    private Long userId;

    /**
     * Создает CardResponse из сущности Card.
     *
     * @param card сущность карты
     * @return DTO с данными карты
     */

    public static CardResponse fromEntity(Card card) {
        CardResponse response = new CardResponse();
        response.setId(card.getId());
        response.setCardNumberMasked(card.getCardNumberMasked());
        response.setCardHolderName(card.getCardHolderName());
        response.setExpirationDate(card.getExpirationDate());
        response.setStatus(card.getStatus());
        response.setBalance(card.getBalance());
        response.setUserId(card.getUser().getId());
        return response;
    }
}