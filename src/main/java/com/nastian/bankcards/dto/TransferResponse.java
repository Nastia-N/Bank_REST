package com.nastian.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO для ответа на перевод средств между картами.
 * <p>
 * Возвращается после успешного выполнения перевода.
 * Содержит информацию о совершенной транзакции:
 * <ul>
 *   <li>ID перевода и подтверждающее сообщение</li>
 *   <li>Сумма перевода</li>
 *   <li>ID карт отправителя и получателя</li>
 *   <li>Временная метка операции</li>
 * </ul>
 */

@Data
@AllArgsConstructor
@Schema(description = "Ответ на перевод средств")
public class TransferResponse {

    @Schema(description = "Сообщение о результате", example = "Transfer completed successfully")
    private String message;

    @Schema(description = "ID перевода", example = "1")
    private Long transferId;

    @Schema(description = "Сумма перевода", example = "1000.50")
    private BigDecimal amount;

    @Schema(description = "ID карты отправителя", example = "1")
    private Long fromCardId;

    @Schema(description = "ID карты получателя", example = "2")
    private Long toCardId;

    @Schema(description = "Время перевода", example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;
}
