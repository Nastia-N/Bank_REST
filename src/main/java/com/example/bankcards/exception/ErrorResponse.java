package com.example.bankcards.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private Map<String, String> validationErrors;

    // Конструктор для простых ошибок
    public ErrorResponse(String message) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
    }

    // Статический фабричный метод
    public static ErrorResponse of(String message) {
        return new ErrorResponse(message);
    }
}
