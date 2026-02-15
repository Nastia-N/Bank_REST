package com.nastian.bankcards.dto;

import com.nastian.bankcards.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO для запроса на регистрацию нового пользователя.
 * <p>
 * Содержит данные, необходимые для создания учетной записи:
 * <ul>
 *   <li>Имя пользователя (уникальное)</li>
 *   <li>Email (уникальный)</li>
 *   <li>Пароль</li>
 *   <li>Роль (по умолчанию USER)</li>
 * </ul>
 */

@Data
@Schema(description = "Запрос на регистрацию пользователя")
public class AuthRequest {

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "Имя пользователя (уникальное)",
            example = "john_doe",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 3,
            maxLength = 50)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "Email пользователя (уникальный)",
            example = "john@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    @Schema(description = "Пароль (минимум 6 символов)",
            example = "password123",
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 6)
    private String password;

    @Schema(description = "Роль пользователя (по умолчанию USER)",
            example = "USER",
            defaultValue = "USER")
    private UserRole role = UserRole.USER;
}