package com.example.bankcards.service;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    private AuthRequest authRequest;
    private User user;

    @BeforeEach
    void setUp() {
        authRequest = new AuthRequest();
        authRequest.setUsername("tester");
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("password123");
        authRequest.setRole(UserRole.USER);

        user = new User();
        user.setId(1L);
        user.setUsername("tester");
        user.setEmail("test@example.com");
        user.setRole(UserRole.USER);
    }

    @Test
    @DisplayName("Регистрация через AuthService - успешно")
    void register_Success() {
        when(userService.register(any(AuthRequest.class))).thenReturn(user);
        User result = authService.register(authRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("tester");
        verify(userService).register(authRequest);
    }

    @Test
    @DisplayName("Регистрация - проброс исключения из UserService")
    void register_ThrowsException() {
        when(userService.register(any(AuthRequest.class)))
                .thenThrow(new IllegalArgumentException("Username already exists"));

        assertThatThrownBy(() -> authService.register(authRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Username already exists");
    }

    @Test
    @DisplayName("Поиск пользователя по username - успешно")
    void findByUsername_Success() {
        when(userService.findByUsername("tester")).thenReturn(user);
        User result = authService.findByUsername("tester");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("tester");
        verify(userService).findByUsername("tester");
    }

    @Test
    @DisplayName("Поиск пользователя по username - не найден")
    void findByUsername_NotFound() {
        when(userService.findByUsername("unknown"))
                .thenThrow(new IllegalArgumentException("User not found"));

        assertThatThrownBy(() -> authService.findByUsername("unknown"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("User not found");
    }
}
