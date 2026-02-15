package com.nastian.bankcards.dto;

import com.nastian.bankcards.entity.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для ответа с JWT токеном после успешной аутентификации.
 * <p>
 * Содержит:
 * <ul>
 *   <li>JWT токен для последующих запросов</li>
 *   <li>Тип токена (Bearer)</li>
 *   <li>Информацию о пользователе (ID, username, email, роль)</li>
 * </ul>
 */

@Data
@AllArgsConstructor
@Schema(description = "Ответ с JWT токеном после успешной аутентификации")
public class JwtResponse {

    @Schema(description = "JWT токен для доступа к защищенным ресурсам",
            example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;

    @Schema(description = "Тип токена (всегда Bearer)",
            example = "Bearer",
            defaultValue = "Bearer",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String type = "Bearer";

    @Schema(description = "ID пользователя",
            example = "1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @Schema(description = "Имя пользователя",
            example = "john_doe",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(description = "Email пользователя",
            example = "john@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Schema(description = "Роль пользователя",
            example = "USER",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private UserRole role;

    public JwtResponse(String token, Long id, String username, String email, UserRole role) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
    }
}