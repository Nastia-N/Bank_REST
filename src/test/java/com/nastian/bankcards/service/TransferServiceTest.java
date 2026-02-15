package com.nastian.bankcards.service;

import com.nastian.bankcards.dto.TransferRequest;
import com.nastian.bankcards.entity.Card;
import com.nastian.bankcards.entity.CardStatus;
import com.nastian.bankcards.entity.Transfer;
import com.nastian.bankcards.entity.TransferStatus;
import com.nastian.bankcards.entity.User;
import com.nastian.bankcards.entity.UserRole;
import com.nastian.bankcards.exception.CardNotActiveException;
import com.nastian.bankcards.exception.InsufficientFundsException;
import com.nastian.bankcards.exception.UnauthorizedAccessException;
import com.nastian.bankcards.repository.CardRepository;
import com.nastian.bankcards.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты сервиса переводов")
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardService cardService;

    @InjectMocks
    private TransferService transferService;

    private Card fromCard;
    private Card toCard;
    private TransferRequest validRequest;
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(USER_ID);
        user.setUsername("tester");
        user.setRole(UserRole.USER);

        fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setUser(user);
        fromCard.setBalance(new BigDecimal("200.00"));
        fromCard.setStatus(CardStatus.ACTIVE);
        fromCard.setExpirationDate(LocalDate.now().plusYears(1));

        toCard = new Card();
        toCard.setId(2L);
        toCard.setUser(user);
        toCard.setBalance(new BigDecimal("50.00"));
        toCard.setStatus(CardStatus.ACTIVE);
        toCard.setExpirationDate(LocalDate.now().plusYears(1));

        validRequest = new TransferRequest();
        validRequest.setFromCardId(1L);
        validRequest.setToCardId(2L);
        validRequest.setAmount(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Успешный перевод между своими картами")
    void transferBetweenOwnCards_Success() {
        when(cardService.getCardAndValidateOwnership(1L, USER_ID)).thenReturn(fromCard);
        when(cardService.getCardAndValidateOwnership(2L, USER_ID)).thenReturn(toCard);
        doNothing().when(cardService).validateCardActive(fromCard);
        doNothing().when(cardService).validateCardActive(toCard);
        when(cardRepository.save(any(Card.class))).thenReturn(fromCard, toCard);
        when(transferRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Transfer result = transferService.transferBetweenOwnCards(validRequest, USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(result.getStatus()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(fromCard.getBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(toCard.getBalance()).isEqualByComparingTo(new BigDecimal("150.00"));

        verify(cardService, times(2)).getCardAndValidateOwnership(anyLong(), anyLong());
        verify(cardService, times(2)).validateCardActive(any(Card.class));
        verify(cardRepository, times(2)).save(any(Card.class));
        verify(transferRepository, times(1)).save(any(Transfer.class));
    }

    @Test
    @DisplayName("Ошибка при недостатке средств")
    void transferBetweenOwnCards_InsufficientFunds() {
        validRequest.setAmount(new BigDecimal("300.00"));
        when(cardService.getCardAndValidateOwnership(1L, USER_ID)).thenReturn(fromCard);
        when(cardService.getCardAndValidateOwnership(2L, USER_ID)).thenReturn(toCard);
        doNothing().when(cardService).validateCardActive(fromCard);
        doNothing().when(cardService).validateCardActive(toCard);

        assertThatThrownBy(() -> transferService.transferBetweenOwnCards(validRequest, USER_ID))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining(String.valueOf(fromCard.getId()));

        verify(cardRepository, never()).save(any());
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ошибка при переводе на ту же карту")
    void transferBetweenOwnCards_SameCard() {
        validRequest.setToCardId(1L);

        when(cardService.getCardAndValidateOwnership(1L, USER_ID)).thenReturn(fromCard);

        assertThatThrownBy(() -> transferService.transferBetweenOwnCards(validRequest, USER_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot transfer money to the same card");

        verify(cardRepository, never()).save(any());
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ошибка при неактивной карте отправителя")
    void transferBetweenOwnCards_FromCardNotActive() {
        fromCard.setStatus(CardStatus.BLOCKED);
        when(cardService.getCardAndValidateOwnership(1L, USER_ID)).thenReturn(fromCard);
        when(cardService.getCardAndValidateOwnership(2L, USER_ID)).thenReturn(toCard);
        doThrow(new CardNotActiveException("Card is not active"))
                .when(cardService).validateCardActive(fromCard);

        assertThatThrownBy(() -> transferService.transferBetweenOwnCards(validRequest, USER_ID))
                .isInstanceOf(CardNotActiveException.class);

        verify(cardRepository, never()).save(any());
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ошибка при чужой карте отправителя")
    void transferBetweenOwnCards_FromCardUnauthorized() {
        when(cardService.getCardAndValidateOwnership(1L, USER_ID))
                .thenThrow(new UnauthorizedAccessException(USER_ID, "card 1"));

        assertThatThrownBy(() -> transferService.transferBetweenOwnCards(validRequest, USER_ID))
                .isInstanceOf(UnauthorizedAccessException.class);

        verify(cardRepository, never()).save(any());
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ошибка при неактивной карте получателя")
    void transferBetweenOwnCards_ToCardNotActive() {
        toCard.setStatus(CardStatus.BLOCKED);
        when(cardService.getCardAndValidateOwnership(1L, USER_ID)).thenReturn(fromCard);
        when(cardService.getCardAndValidateOwnership(2L, USER_ID)).thenReturn(toCard);
        doNothing().when(cardService).validateCardActive(fromCard);
        doThrow(new CardNotActiveException("Card is not active"))
                .when(cardService).validateCardActive(toCard);

        assertThatThrownBy(() -> transferService.transferBetweenOwnCards(validRequest, USER_ID))
                .isInstanceOf(CardNotActiveException.class);

        verify(cardRepository, never()).save(any());
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ошибка при чужой карте получателя")
    void transferBetweenOwnCards_ToCardUnauthorized() {
        when(cardService.getCardAndValidateOwnership(1L, USER_ID)).thenReturn(fromCard);
        when(cardService.getCardAndValidateOwnership(2L, USER_ID))
                .thenThrow(new UnauthorizedAccessException(USER_ID, "card 2"));

        assertThatThrownBy(() -> transferService.transferBetweenOwnCards(validRequest, USER_ID))
                .isInstanceOf(UnauthorizedAccessException.class);

        verify(cardRepository, never()).save(any());
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ошибка при просроченной карте")
    void transferBetweenOwnCards_ExpiredCard() {
        fromCard.setExpirationDate(LocalDate.now().minusDays(1));
        when(cardService.getCardAndValidateOwnership(1L, USER_ID)).thenReturn(fromCard);
        when(cardService.getCardAndValidateOwnership(2L, USER_ID)).thenReturn(toCard);
        doThrow(new CardNotActiveException("Card has expired"))
                .when(cardService).validateCardActive(fromCard);

        assertThatThrownBy(() -> transferService.transferBetweenOwnCards(validRequest, USER_ID))
                .isInstanceOf(CardNotActiveException.class)
                .hasMessageContaining("expired");

        verify(cardRepository, never()).save(any());
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("Проверка создания записи о переводе")
    void transferBetweenOwnCards_TransferRecordCreated() {
        when(cardService.getCardAndValidateOwnership(1L, USER_ID)).thenReturn(fromCard);
        when(cardService.getCardAndValidateOwnership(2L, USER_ID)).thenReturn(toCard);
        doNothing().when(cardService).validateCardActive(fromCard);
        doNothing().when(cardService).validateCardActive(toCard);
        when(cardRepository.save(any(Card.class))).thenReturn(fromCard, toCard);
        when(transferRepository.save(any())).thenAnswer(invocation -> {
            Transfer transfer = invocation.getArgument(0);
            transfer.setId(100L);
            return transfer;
        });

        Transfer result = transferService.transferBetweenOwnCards(validRequest, USER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(TransferStatus.COMPLETED);
        assertThat(result.getFromCard()).isEqualTo(fromCard);
        assertThat(result.getToCard()).isEqualTo(toCard);

        verify(transferRepository, times(1)).save(any(Transfer.class));
    }
}