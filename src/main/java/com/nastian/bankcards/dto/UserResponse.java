package com.nastian.bankcards.dto;

import com.nastian.bankcards.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO для ответа с данными пользователя.
 * <p>
 * Содержит информацию о пользователе для отображения:
 * <ul>
 *   <li>ID пользователя</li>
 *   <li>Имя пользователя</li>
 *   <li>Email</li>
 *   <li>Роль (USER/ADMIN)</li>
 *   <li>Дата регистрации</li>
 * </ul>
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ответ с данными пользователя")
public class UserResponse {

    @Schema(description = "ID пользователя",
            example = "1")
    private Long id;

    @Schema(description = "Имя пользователя (уникальное)",
            example = "john_doe")
    private String username;

    @Schema(description = "Email пользователя (уникальный)",
            example = "john@example.com")
    private String email;

    @Schema(description = "Роль пользователя в системе",
            example = "USER")
    private UserRole role;

    @Schema(description = "Дата и время регистрации",
            example = "2024-01-15T10:30:00")
    private LocalDateTime createdAt;
}