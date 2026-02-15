package com.nastian.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * DTO для запроса на перевод средств между картами.
 * <p>
 * Содержит данные для выполнения перевода:
 * <ul>
 *   <li>ID карты-отправителя</li>
 *   <li>ID карты-получателя</li>
 *   <li>Сумма перевода</li>
 * </ul>
 */

@Data
@Schema(description = "Запрос на перевод средств между картами")
public class TransferRequest {

    @NotNull(message = "From card ID is required")
    @Schema(description = "ID карты, с которой списываются средства",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long fromCardId;

    @NotNull(message = "To card ID is required")
    @Schema(description = "ID карты, на которую зачисляются средства",
            example = "2",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long toCardId;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Schema(description = "Сумма перевода (минимум 0.01)",
            example = "1000.50",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minimum = "0.01")
    private BigDecimal amount;
}