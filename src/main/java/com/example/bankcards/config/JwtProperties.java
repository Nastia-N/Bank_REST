package com.example.bankcards.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    private String secret;
    private long expiration = 86400000; // 24 часа
    private String header = "Authorization";
    private String prefix = "Bearer ";
}