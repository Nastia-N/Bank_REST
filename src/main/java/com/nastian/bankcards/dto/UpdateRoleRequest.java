package com.nastian.bankcards.dto;

import com.nastian.bankcards.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * DTO для запроса на изменение роли пользователя.
 * <p>
 * Используется администратором для изменения роли пользователя.
 * Доступные роли определяются enum UserRole: USER, ADMIN.
 */

@Data
@Schema(description = "Запрос на изменение роли пользователя")
public class UpdateRoleRequest {

    @NotNull(message = "Роль не может быть null")
    @Schema(description = "Новая роль пользователя",
            example = "ADMIN")
    private UserRole role;
}