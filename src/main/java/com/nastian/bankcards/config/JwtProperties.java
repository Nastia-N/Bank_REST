package com.nastian.bankcards.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Настройки JWT из application.yml.
 * <p>
 * Содержит: секретный ключ для подписи токенов, время жизни токена,
 * заголовок авторизации и префикс токена.
 *
 */

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    private String secret;
    private long expiration = 86400000;
    private String header = "Authorization";
    private String prefix = "Bearer ";
}