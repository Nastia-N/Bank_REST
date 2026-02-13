package com.example.bankcards.service;

import com.example.bankcards.dto.AuthRequest;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.DuplicateResourceException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

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
        user.setPassword("encodedPassword");
        user.setRole(UserRole.USER);
    }

    @Test
    @DisplayName("Регистрация нового пользователя - успешно")
    void register_Success() {

        when(userRepository.existsByUsername("tester")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.register(authRequest);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("tester");
        assertThat(result.getEmail()).isEqualTo("test@example.com");
        assertThat(result.getRole()).isEqualTo(UserRole.USER);

        verify(userRepository).existsByUsername("tester");
        verify(userRepository).existsByEmail("test@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Регистрация - username уже существует")
    void register_UsernameExists() {
        when(userRepository.existsByUsername("tester")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(authRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Username already exists with value: tester");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Регистрация - email уже существует")
    void register_EmailExists() {
        when(userRepository.existsByUsername("tester")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.register(authRequest))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Email already exists with value: test@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Регистрация администратора - успешно")
    void register_Admin_Success() {
        authRequest.setRole(UserRole.ADMIN);
        when(userRepository.existsByUsername("tester")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.register(authRequest);

        assertThat(result).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Поиск пользователя по ID - успешно")
    void findById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Поиск пользователя по ID - не найден")
    void findById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");
    }

    @Test
    @DisplayName("Поиск пользователя по username - успешно")
    void findByUsername_Success() {
        when(userRepository.findByUsername("tester")).thenReturn(Optional.of(user));

        User result = userService.findByUsername("tester");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("tester");
    }

    @Test
    @DisplayName("Поиск пользователя по username - не найден")
    void findByUsername_NotFound() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findByUsername("unknown"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with username: unknown");
    }

    @Test
    @DisplayName("Проверка существования username - существует")
    void existsByUsername_True() {
        when(userRepository.existsByUsername("tester")).thenReturn(true);
        boolean result = userService.existsByUsername("tester");
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Проверка существования username - не существует")
    void existsByUsername_False() {
        when(userRepository.existsByUsername("unknown")).thenReturn(false);
        boolean result = userService.existsByUsername("unknown");
        assertThat(result).isFalse();
    }
}
