package com.nastian.bankcards.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * DTO для единообразного формата ответов с ошибками.
 * <p>
 * Используется во всем приложении для возврата информации об ошибках.
 * Содержит временную метку, HTTP-статус, сообщение об ошибке и путь запроса.
 * Поле validationErrors заполняется только при ошибках валидации.
 */

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Ответ с информацией об ошибке")
public class ErrorResponse {

    @Schema(description = "Время возникновения ошибки", example = "2024-01-15T10:30:00")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP статус код", example = "400")
    private int status;

    @Schema(description = "Тип ошибки", example = "Bad Request")
    private String error;

    @Schema(description = "Сообщение об ошибке", example = "User not found with id: 1")
    private String message;

    @Schema(description = "Путь запроса", example = "/api/users/1")
    private String path;

    @Schema(description = "Детальные ошибки валидации (поле -> сообщение)",
            example = "{\"username\": \"Username is required\", \"email\": \"Invalid email format\"}")
    private Map<String, String> validationErrors;

    public ErrorResponse(String message) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
    }

    public static ErrorResponse of(String message) {
        return new ErrorResponse(message);
    }
}