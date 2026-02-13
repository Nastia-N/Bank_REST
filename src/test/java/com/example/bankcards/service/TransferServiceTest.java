package com.example.bankcards.service;

import com.example.bankcards.dto.TransferRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.exception.CardNotActiveException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.exception.UnauthorizedAccessException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
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

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(1L);
        user.setUsername("tester");
        user.setRole(UserRole.USER);

        fromCard = new Card();
        fromCard.setId(1L);
        fromCard.setUser(user);
        fromCard.setBalance(new BigDecimal("200.00"));
        fromCard.setStatus("ACTIVE");
        fromCard.setExpirationDate(LocalDate.now().plusYears(1));

        toCard = new Card();
        toCard.setId(2L);
        toCard.setUser(user);
        toCard.setBalance(new BigDecimal("50.00"));
        toCard.setStatus("ACTIVE");
        toCard.setExpirationDate(LocalDate.now().plusYears(1));

        validRequest = new TransferRequest();
        validRequest.setFromCardId(1L);
        validRequest.setToCardId(2L);
        validRequest.setAmount(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Успешный перевод между своими картами")
    void transferBetweenOwnCards_Success() {

        when(cardService.getCardAndValidateOwnership(1L, 1L)).thenReturn(fromCard);
        when(cardService.getCardAndValidateOwnership(2L, 1L)).thenReturn(toCard);
        doNothing().when(cardService).validateCardActive(fromCard);
        doNothing().when(cardService).validateCardActive(toCard);
        when(cardRepository.save(any(Card.class))).thenReturn(fromCard, toCard);
        when(transferRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var result = transferService.transferBetweenOwnCards(validRequest, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(fromCard.getBalance()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(toCard.getBalance()).isEqualByComparingTo(new BigDecimal("150.00"));

        verify(cardService, times(2)).getCardAndValidateOwnership(anyLong(), anyLong());
        verify(cardService, times(2)).validateCardActive(any(Card.class));
        verify(cardRepository, times(2)).save(any(Card.class));
        verify(transferRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Ошибка при недостатке средств")
    void transferBetweenOwnCards_InsufficientFunds() {

        validRequest.setAmount(new BigDecimal("300.00"));
        when(cardService.getCardAndValidateOwnership(1L, 1L)).thenReturn(fromCard);
        when(cardService.getCardAndValidateOwnership(2L, 1L)).thenReturn(toCard);
        doNothing().when(cardService).validateCardActive(fromCard);
        doNothing().when(cardService).validateCardActive(toCard);

        assertThatThrownBy(() -> transferService.transferBetweenOwnCards(validRequest, 1L))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Insufficient funds on card 1");

        verify(cardRepository, never()).save(any());
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ошибка при переводе на ту же карту")
    void transferBetweenOwnCards_SameCard() {

        validRequest.setToCardId(1L);
        when(cardService.getCardAndValidateOwnership(1L, 1L)).thenReturn(fromCard);
        when(cardService.getCardAndValidateOwnership(1L, 1L)).thenReturn(fromCard);

        assertThatThrownBy(() -> transferService.transferBetweenOwnCards(validRequest, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot transfer money to the same card");

        verify(cardRepository, never()).save(any());
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ошибка при неактивной карте отправителя")
    void transferBetweenOwnCards_FromCardNotActive() {

        fromCard.setStatus("BLOCKED");
        when(cardService.getCardAndValidateOwnership(1L, 1L)).thenReturn(fromCard);
        when(cardService.getCardAndValidateOwnership(2L, 1L)).thenReturn(toCard);
        doThrow(new CardNotActiveException("Card is not active"))
                .when(cardService).validateCardActive(fromCard);

        assertThatThrownBy(() -> transferService.transferBetweenOwnCards(validRequest, 1L))
                .isInstanceOf(CardNotActiveException.class);

        verify(cardRepository, never()).save(any());
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("Ошибка при чужой карте отправителя")
    void transferBetweenOwnCards_FromCardUnauthorized() {

        User otherUser = new User();
        otherUser.setId(999L);
        fromCard.setUser(otherUser);

        when(cardService.getCardAndValidateOwnership(1L, 1L))
                .thenThrow(new UnauthorizedAccessException(1L, "card 1"));

        assertThatThrownBy(() -> transferService.transferBetweenOwnCards(validRequest, 1L))
                .isInstanceOf(UnauthorizedAccessException.class);

        verify(cardRepository, never()).save(any());
        verify(transferRepository, never()).save(any());
    }
}
