package com.nastian.bankcards.service;

import com.nastian.bankcards.dto.CardRequest;
import com.nastian.bankcards.entity.Card;
import com.nastian.bankcards.entity.CardStatus;
import com.nastian.bankcards.entity.User;
import com.nastian.bankcards.entity.UserRole;
import com.nastian.bankcards.exception.CardNotActiveException;
import com.nastian.bankcards.exception.CardNotFoundException;
import com.nastian.bankcards.exception.UnauthorizedAccessException;
import com.nastian.bankcards.exception.UserNotFoundException;
import com.nastian.bankcards.repository.CardRepository;
import com.nastian.bankcards.repository.UserRepository;
import com.nastian.bankcards.util.CardNumberGenerator;
import com.nastian.bankcards.util.CardNumberMasker;
import com.nastian.bankcards.util.EncryptionUtil;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса операций с картами")
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
    private static final Long USER_ID = 1L;
    private static final Long CARD_ID = 1L;
    private static final Long OTHER_USER_ID = 999L;
    private static final Long OTHER_CARD_ID = 999L;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setUsername("tester");
        user.setRole(UserRole.USER);

        card = new Card();
        card.setId(CARD_ID);
        card.setUser(user);
        card.setCardHolderName("Test User");
        card.setExpirationDate(LocalDate.now().plusYears(1));
        card.setStatus(CardStatus.ACTIVE);
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
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(cardNumberGenerator.generate()).thenReturn("1234567890123456");
        when(encryptionUtil.encrypt("1234567890123456")).thenReturn("encrypted123");
        when(cardNumberMasker.mask("1234567890123456")).thenReturn("**** **** **** 3456");
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        Card result = cardService.createCard(cardRequest, USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getCardHolderName()).isEqualTo("Test User");
        assertThat(result.getStatus()).isEqualTo(CardStatus.ACTIVE);
        assertThat(result.getBalance()).isEqualByComparingTo(BigDecimal.ZERO);

        verify(userRepository).findById(USER_ID);
        verify(cardNumberGenerator).generate();
        verify(encryptionUtil).encrypt(anyString());
        verify(cardNumberMasker).mask(anyString());
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Создание карты - пользователь не найден")
    void createCard_UserNotFound() {
        when(userRepository.findById(OTHER_USER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.createCard(cardRequest, OTHER_USER_ID))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining(String.valueOf(OTHER_USER_ID));

        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("Получение карт пользователя - успешно")
    void getUserCards_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> page = new PageImpl<>(List.of(card));

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(cardRepository.findByUserId(USER_ID, pageable)).thenReturn(page);

        Page<Card> result = cardService.getUserCards(USER_ID, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getCardHolderName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Получение карт пользователя с поиском - успешно")
    void getUserCards_WithSearch_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> page = new PageImpl<>(List.of(card));

        when(userRepository.existsById(USER_ID)).thenReturn(true);
        when(cardRepository.findByUserIdAndCardNumberMaskedContaining(eq(USER_ID), eq("1234"), eq(pageable)))
                .thenReturn(page);

        Page<Card> result = cardService.getUserCards(USER_ID, "1234", pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Получение карт пользователя - пользователь не найден")
    void getUserCards_UserNotFound() {
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.existsById(OTHER_USER_ID)).thenReturn(false);

        assertThatThrownBy(() -> cardService.getUserCards(OTHER_USER_ID, null, pageable))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Проверка владения картой - успешно")
    void getCardAndValidateOwnership_Success() {
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        Card result = cardService.getCardAndValidateOwnership(CARD_ID, USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(CARD_ID);
    }

    @Test
    @DisplayName("Проверка владения картой - карта не найдена")
    void getCardAndValidateOwnership_CardNotFound() {
        when(cardRepository.findById(OTHER_CARD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardAndValidateOwnership(OTHER_CARD_ID, USER_ID))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessageContaining(String.valueOf(OTHER_CARD_ID));
    }

    @Test
    @DisplayName("Проверка владения картой - доступ запрещен")
    void getCardAndValidateOwnership_Unauthorized() {
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.getCardAndValidateOwnership(CARD_ID, OTHER_USER_ID))
                .isInstanceOf(UnauthorizedAccessException.class)
                .hasMessageContaining(String.valueOf(OTHER_USER_ID));
    }

    @Test
    @DisplayName("Блокировка карты - успешно")
    void blockCard_Success() {
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        Card result = cardService.blockCard(CARD_ID, USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(CardStatus.BLOCKED);
    }

    @Test
    @DisplayName("Блокировка карты - карта уже заблокирована")
    void blockCard_AlreadyBlocked() {
        card.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.blockCard(CARD_ID, USER_ID))
                .isInstanceOf(CardNotActiveException.class)
                .hasMessageContaining("already blocked");
    }

    @Test
    @DisplayName("Активация карты - успешно")
    void activateCard_Success() {
        card.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(cardRepository.save(any(Card.class))).thenReturn(card);

        Card result = cardService.activateCard(CARD_ID, USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(CardStatus.ACTIVE);
    }

    @Test
    @DisplayName("Активация карты - карта уже активна")
    void activateCard_AlreadyActive() {
        card.setStatus(CardStatus.ACTIVE);
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.activateCard(CARD_ID, USER_ID))
                .isInstanceOf(CardNotActiveException.class)
                .hasMessageContaining("already active");
    }

    @Test
    @DisplayName("Активация карты - карта просрочена")
    void activateCard_Expired() {
        card.setStatus(CardStatus.BLOCKED);
        card.setExpirationDate(LocalDate.now().minusDays(1));
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

        assertThatThrownBy(() -> cardService.activateCard(CARD_ID, USER_ID))
                .isInstanceOf(CardNotActiveException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("Получение баланса - успешно")
    void getCardBalance_Success() {
        card.setBalance(new BigDecimal("100.00"));
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));

        BigDecimal result = cardService.getCardBalance(CARD_ID, USER_ID);
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
        card.setStatus(CardStatus.BLOCKED);
        assertThatThrownBy(() -> cardService.validateCardActive(card))
                .isInstanceOf(CardNotActiveException.class)
                .hasMessageContaining("is not active");
    }

    @Test
    @DisplayName("Проверка активности карты - карта просрочена")
    void validateCardActive_Expired() {
        card.setExpirationDate(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> cardService.validateCardActive(card))
                .isInstanceOf(CardNotActiveException.class)
                .hasMessageContaining("expired");
    }

    @Test
    @DisplayName("Получение баланса - карта не найдена")
    void getCardBalance_CardNotFound() {
        when(cardRepository.findById(OTHER_CARD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cardService.getCardBalance(OTHER_CARD_ID, USER_ID))
                .isInstanceOf(CardNotFoundException.class)
                .hasMessageContaining(String.valueOf(OTHER_CARD_ID));
    }
}