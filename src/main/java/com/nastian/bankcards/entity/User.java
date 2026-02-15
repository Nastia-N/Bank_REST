package com.nastian.bankcards.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Сущность пользователя системы.
 * <p>
 * Содержит учетные данные пользователя:
 * <ul>
 *   <li>Уникальное имя пользователя (для входа)</li>
 *   <li>Уникальный email</li>
 *   <li>Зашифрованный пароль</li>
 *   <li>Роль (USER/ADMIN)</li>
 *   <li>Дата регистрации</li>
 * </ul>
 */

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_user_username", columnList = "username")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}