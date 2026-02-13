package com.example.bankcards.service;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardService cardService;

    @InjectMocks
    private AdminService adminService;

    private User user;
    private User admin;
    private Card card;
    private CardRequest cardRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("tester");
        user.setEmail("user@test.com");
        user.setRole(UserRole.USER);

        admin = new User();
        admin.setId(2L);
        admin.setUsername("admin");
        admin.setEmail("admin@test.com");
        admin.setRole(UserRole.ADMIN);

        card = new Card();
        card.setId(1L);
        card.setUser(user);
        card.setCardHolderName("Test User");
        card.setStatus("ACTIVE");

        cardRequest = new CardRequest();
        cardRequest.setCardHolderName("New Card");
        cardRequest.setExpirationDate(LocalDate.now().plusYears(1));

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Получение всех пользователей - успешно")
    void getAllUsers_Success() {
        Page<User> page = new PageImpl<>(List.of(user, admin));
        when(userRepository.findAll(pageable)).thenReturn(page);

        Page<User> result = adminService.getAllUsers(null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        verify(userRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Поиск пользователей - успешно")
    void getAllUsers_WithSearch_Success() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.searchUsers("test", pageable)).thenReturn(page);

        Page<User> result = adminService.getAllUsers("test", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(userRepository).searchUsers("test", pageable);
        verify(userRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    @DisplayName("Получение пользователя по ID - успешно")
    void getUserById_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        User result = adminService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("tester");
    }

    @Test
    @DisplayName("Получение пользователя по ID - не найден")
    void getUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> adminService.getUserById(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");
    }

    @Test
    @DisplayName("Изменение роли пользователя - успешно")
    void updateUserRole_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = adminService.updateUserRole(1L, UserRole.ADMIN);

        assertThat(result).isNotNull();
        assertThat(result.getRole()).isEqualTo(UserRole.ADMIN);
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Изменение роли пользователя - пользователь не найден")
    void updateUserRole_UserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> adminService.updateUserRole(999L, UserRole.ADMIN))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Удаление пользователя - успешно")
    void deleteUser_Success() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        adminService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Удаление пользователя - не найден")
    void deleteUser_NotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);
        assertThatThrownBy(() -> adminService.deleteUser(999L))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Получение всех карт - успешно")
    void getAllCards_Success() {
        Page<Card> page = new PageImpl<>(List.of(card));
        when(cardRepository.findAll(pageable)).thenReturn(page);

        Page<Card> result = adminService.getAllCards(null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(cardRepository).findAll(pageable);
    }

    @Test
    @DisplayName("Поиск карт - успешно")
    void getAllCards_WithSearch_Success() {
        Page<Card> page = new PageImpl<>(List.of(card));
        when(cardRepository.findAllByCardNumberMaskedContaining("1234", pageable)).thenReturn(page);

        Page<Card> result = adminService.getAllCards("1234", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        verify(cardRepository).findAllByCardNumberMaskedContaining("1234", pageable);
    }

    @Test
    @DisplayName("Получение карт пользователя - успешно")
    void getUserCards_Success() {
        Page<Card> page = new PageImpl<>(List.of(card));
        when(userRepository.existsById(1L)).thenReturn(true);
        when(cardRepository.findByUserId(1L, pageable)).thenReturn(page);

        Page<Card> result = adminService.getUserCards(1L, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Получение карт пользователя - пользователь не найден")
    void getUserCards_UserNotFound() {
        when(userRepository.existsById(999L)).thenReturn(false);
        assertThatThrownBy(() -> adminService.getUserCards(999L, null, pageable))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Создание карты для пользователя - успешно")
    void createCardForUser_Success() {
        when(cardService.createCard(cardRequest, 1L)).thenReturn(card);
        Card result = adminService.createCardForUser(1L, cardRequest);

        assertThat(result).isNotNull();
        verify(cardService).createCard(cardRequest, 1L);
    }

    @Test
    @DisplayName("Изменение статуса карты - успешно")
    void updateCardStatus_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        Card result = adminService.updateCardStatus(1L, "BLOCKED");

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("BLOCKED");
    }

    @Test
    @DisplayName("Изменение статуса карты - карта не найдена")
    void updateCardStatus_CardNotFound() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> adminService.updateCardStatus(999L, "BLOCKED"))
                .isInstanceOf(CardNotFoundException.class);
    }

    @Test
    @DisplayName("Изменение статуса карты - неверный статус")
    void updateCardStatus_InvalidStatus() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        assertThatThrownBy(() -> adminService.updateCardStatus(1L, "INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid status");
    }

    @Test
    @DisplayName("Удаление карты - успешно")
    void deleteCard_Success() {
        when(cardRepository.existsById(1L)).thenReturn(true);
        doNothing().when(cardRepository).deleteById(1L);
        adminService.deleteCard(1L);
        verify(cardRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Удаление карты - карта не найдена")
    void deleteCard_NotFound() {
        when(cardRepository.existsById(999L)).thenReturn(false);
        assertThatThrownBy(() -> adminService.deleteCard(999L))
                .isInstanceOf(CardNotFoundException.class);
    }
}
