package com.example.bankcards.service;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardNotActiveException;
import com.example.bankcards.exception.UnauthorizedAccessException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.CardNumberGenerator;
import com.example.bankcards.util.CardNumberMasker;
import com.example.bankcards.util.EncryptionUtil;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private CardNumberGenerator cardNumberGenerator;

    @Mock
    private CardNumberMasker cardNumberMasker;

    @InjectMocks
    private CardService cardService;

    private User user;
    private Card card;
    private CardRequest cardRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setRole(UserRole.USER);

        card = new Card();
        card.setId(1L);
        card.setUser(user);
        card.setCardHolderName("Test User");
        card.setExpirationDate(LocalDate.now().plusYears(1));
        card.setStatus("ACTIVE");
        card.setBalance(BigDecimal.ZERO);
        card.setCardNumberEncrypted("encrypted123");
        card.setCardNumberMasked("**** **** **** 1234");

        cardRequest = new CardRequest();
        cardRequest.setCardHolderName("Test User");
        cardRequest.setExpirationDate(LocalDate.now().plusYears(1));
    }

    @Test
    @DisplayName("Создание карты - успешно")
    void createCard_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(cardNumberGenerator.generate()).thenReturn("1234567890123456");
        when(encryptionUtil.encrypt("1234567890123456")).thenReturn("encrypted123");
        when(cardNumberMasker.mask("1234567890123456")).thenReturn("**** **** **** 3456");
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        Card result = cardService.createCard(cardRequest, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getCardHolderName()).isEqualTo("Test User");
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(userRepository).findById(1L);
        verify(cardNumberGenerator).generate();
        verify(encryptionUtil).encrypt(anyString());
        verify(cardNumberMasker).mask(anyString());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Создание карты - пользователь не найден")
    void createCard_UserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.createCard(cardRequest, 999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");

        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("Получение карт пользователя - успешно")
    void getUserCards_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> page = new PageImpl<>(List.of(card));

        when(userRepository.existsById(1L)).thenReturn(true);
        when(cardRepository.findByUserId(1L, pageable)).thenReturn(page);

        Page<Card> result = cardService.getUserCards(1L, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCardHolderName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Получение карт пользователя с поиском - успешно")
    void getUserCards_WithSearch_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> page = new PageImpl<>(List.of(card));

        when(userRepository.existsById(1L)).thenReturn(true);
        when(cardRepository.findByUserIdAndCardNumberMaskedContaining(eq(1L), eq("1234"), eq(pageable)))
                .thenReturn(page);

        Page<Card> result = cardService.getUserCards(1L, "1234", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Получение карт пользователя - пользователь не найден")
    void getUserCards_UserNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> cardService.getUserCards(999L, null, pageable))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Проверка владения картой - успешно")
    void getCardAndValidateOwnership_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        Card result = cardService.getCardAndValidateOwnership(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Проверка владения картой - карта не найдена")
    void getCardAndValidateOwnership_CardNotFound() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardAndValidateOwnership(999L, 1L))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessageContaining("Card not found with id: 999");
    }

    @Test
    @DisplayName("Проверка владения картой - доступ запрещен")
    void getCardAndValidateOwnership_Unauthorized() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.getCardAndValidateOwnership(1L, 999L))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining("User 999 does not have access to card 1");
    }

    @Test
    @DisplayName("Блокировка карты - успешно")
    void blockCard_Success() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        Card result = cardService.blockCard(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("BLOCKED");
    }

    @Test
    @DisplayName("Блокировка карты - карта уже заблокирована")
    void blockCard_AlreadyBlocked() {
        card.setStatus("BLOCKED");
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.blockCard(1L, 1L))
                .isInstanceOf(CardNotActiveException.class)
                .hasMessageContaining("Card is already blocked");
    }

    @Test
    @DisplayName("Активация карты - успешно")
    void activateCard_Success() {
        card.setStatus("BLOCKED");
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        Card result = cardService.activateCard(1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("Активация карты - карта уже активна")
    void activateCard_AlreadyActive() {
        card.setStatus("ACTIVE");
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.activateCard(1L, 1L))
                .isInstanceOf(CardNotActiveException.class)
                .hasMessageContaining("Card is already active");
    }

    @Test
    @DisplayName("Активация карты - карта просрочена")
    void activateCard_Expired() {
        card.setStatus("BLOCKED");
        card.setExpirationDate(LocalDate.now().minusDays(1));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.activateCard(1L, 1L))
                .isInstanceOf(CardNotActiveException.class)
                .hasMessageContaining("Cannot activate expired card");
    }

    @Test
    @DisplayName("Получение баланса - успешно")
    void getCardBalance_Success() {
        card.setBalance(new BigDecimal("100.00"));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));

        BigDecimal result = cardService.getCardBalance(1L, 1L);
        assertThat(result).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Проверка активности карты - успешно")
    void validateCardActive_Success() {
        cardService.validateCardActive(card);
    }

    @Test
    @DisplayName("Проверка активности карты - карта неактивна")
    void validateCardActive_NotActive() {
        card.setStatus("BLOCKED");
        assertThatThrownBy(() -> cardService.validateCardActive(card))
                .isInstanceOf(CardNotActiveException.class);
    }

    @Test
    @DisplayName("Проверка активности карты - карта просрочена")
    void validateCardActive_Expired() {
        card.setExpirationDate(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> cardService.validateCardActive(card))
                .isInstanceOf(CardNotActiveException.class)
                .hasMessageContaining("has expired");
    }
}
