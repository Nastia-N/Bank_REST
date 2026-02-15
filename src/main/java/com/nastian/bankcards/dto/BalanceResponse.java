package com.nastian.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

/**
 * DTO для ответа с балансом карты.
 * <p>
 * Используется при запросе баланса конкретной карты.
 * Содержит идентификатор карты и её текущий баланс.
 */

@Data
@AllArgsConstructor
@Schema(description = "Ответ с балансом карты")
public class BalanceResponse {

    @Schema(description = "ID карты", example = "1")
    private Long cardId;

    @Schema(description = "Текущий баланс", example = "1500.50")
    private BigDecimal balance;
}