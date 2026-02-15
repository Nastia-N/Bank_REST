package com.nastian.bankcards.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.lang.annotation.*;

/**
 * Аннотация для внедрения текущего аутентифицированного пользователя
 * в параметры методов контроллера.
 * <p>
 * Использование: {@code @CurrentUser CustomUserDetails currentUser}
 */

@Target({ElementType.PARAMETER, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@AuthenticationPrincipal
public @interface CurrentUser {
}