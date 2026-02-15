package com.nastian.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * DTO для запроса на вход в систему.
 * <p>
 * Содержит учетные данные пользователя:
 * <ul>
 *   <li>Имя пользователя</li>
 *   <li>Пароль</li>
 * </ul>
 */

@Data
@Schema(description = "Запрос на вход в систему")
public class LoginRequest {

    @NotBlank(message = "Username is required")
    @Schema(description = "Имя пользователя",
            example = "john_doe",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Password is required")
    @Schema(description = "Пароль",
            example = "password123",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 6)
    private String password;
}