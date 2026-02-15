package com.nastian.bankcards.service;

import com.nastian.bankcards.dto.AuthRequest;
import com.nastian.bankcards.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Сервис для аутентификации и регистрации.
 * <p>
 * Делегирует выполнение операций UserService.
 */

@Service
public class AuthService {

    private final UserService userService;

    public AuthService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Регистрация нового пользователя.
     *
     * @param request данные для регистрации
     * @return зарегистрированный пользователь
     */
    @Transactional
    public User register(AuthRequest request) {
        return userService.register(request);
    }

    /**
     * Поиск пользователя по имени.
     *
     * @param username имя пользователя
     * @return найденный пользователь
     */
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userService.findByUsername(username);
    }
}