package com.nastian.bankcards.controller;

import com.nastian.bankcards.dto.CardRequest;
import com.nastian.bankcards.dto.TransferRequest;
import com.nastian.bankcards.entity.Card;
import com.nastian.bankcards.entity.CardStatus;
import com.nastian.bankcards.entity.Transfer;
import com.nastian.bankcards.entity.TransferStatus;
import com.nastian.bankcards.entity.User;
import com.nastian.bankcards.entity.UserRole;
import com.nastian.bankcards.security.CustomUserDetails;
import com.nastian.bankcards.security.JwtTokenProvider;
import com.nastian.bankcards.service.CardService;
import com.nastian.bankcards.service.TransferService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@Import(CardControllerTest.TestSecurityConfig.class)
@DisplayName("Тесты контроллера операций с картами")
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    @MockBean
    private TransferService transferService;

    @MockBean
    private JwtTokenProvider tokenProvider;

    private Card card;
    private CardRequest cardRequest;
    private Transfer transfer;
    private TransferRequest transferRequest;
    private CustomUserDetails userDetails;
    private User user;
    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(USER_ID);
        user.setUsername("tester");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole(UserRole.USER);
        user.setCreatedAt(LocalDateTime.now());

        userDetails = new CustomUserDetails(user);

        card = new Card();
        card.setId(1L);
        card.setUser(user);
        card.setCardHolderName("Test User");
        card.setExpirationDate(LocalDate.now().plusYears(1));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(new BigDecimal("100.00"));
        card.setCardNumberMasked("**** **** **** 1234");

        cardRequest = new CardRequest();
        cardRequest.setCardHolderName("Test User");
        cardRequest.setExpirationDate(LocalDate.now().plusYears(1));

        transfer = new Transfer();
        transfer.setId(1L);
        transfer.setFromCard(card);
        transfer.setToCard(card);
        transfer.setAmount(new BigDecimal("50.00"));
        transfer.setStatus(TransferStatus.COMPLETED);
        transfer.setTimestamp(LocalDateTime.now());

        transferRequest = new TransferRequest();
        transferRequest.setFromCardId(1L);
        transferRequest.setToCardId(2L);
        transferRequest.setAmount(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Получение своих карт - успешно")
    void getMyCards_Success() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        PageImpl<Card> page = new PageImpl<>(List.of(card), pageable, 1);
        when(cardService.getUserCards(eq(USER_ID), isNull(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/user/cards")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].cardNumberMasked", is("**** **** **** 1234")))
                .andExpect(jsonPath("$.content[0].cardHolderName", is("Test User")))
                .andExpect(jsonPath("$.content[0].balance", is(100.00)))
                .andExpect(jsonPath("$.content[0].status", is("ACTIVE")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("Получение карт с поиском - успешно")
    void getMyCards_WithSearch_Success() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        PageImpl<Card> page = new PageImpl<>(List.of(card), pageable, 1);
        when(cardService.getUserCards(eq(USER_ID), eq("1234"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/user/cards")
                        .param("search", "1234")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].cardNumberMasked", is("**** **** **** 1234")));
    }

    @Test
    @DisplayName("Создание карты - успешно")
    void createCard_Success() throws Exception {
        when(cardService.createCard(any(CardRequest.class), eq(USER_ID))).thenReturn(card);

        mockMvc.perform(post("/user/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(userDetails))
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.cardHolderName", is("Test User")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    @DisplayName("Создание карты - неверные данные")
    void createCard_InvalidData() throws Exception {
        cardRequest.setCardHolderName("");

        mockMvc.perform(post("/user/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(userDetails))
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).createCard(any(), any());
    }

    @Test
    @DisplayName("Создание карты - срок действия в прошлом")
    void createCard_ExpiredDate() throws Exception {
        cardRequest.setExpirationDate(LocalDate.now().minusDays(1));

        mockMvc.perform(post("/user/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(userDetails))
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isBadRequest());

        verify(cardService, never()).createCard(any(), any());
    }

    @Test
    @DisplayName("Блокировка карты - успешно")
    void blockCard_Success() throws Exception {
        Card blockedCard = new Card();
        blockedCard.setId(1L);
        blockedCard.setStatus(CardStatus.BLOCKED);
        blockedCard.setCardNumberMasked("**** **** **** 1234");
        blockedCard.setUser(user);  // 👈 Добавлено!

        when(cardService.blockCard(1L, USER_ID)).thenReturn(blockedCard);

        mockMvc.perform(post("/user/cards/1/block")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("BLOCKED")));
    }

    @Test
    @DisplayName("Блокировка карты - карта не найдена")
    void blockCard_NotFound() throws Exception {
        when(cardService.blockCard(999L, USER_ID))
                .thenThrow(new IllegalArgumentException("Card not found"));

        mockMvc.perform(post("/user/cards/999/block")
                        .with(user(userDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Активация карты - успешно")
    void activateCard_Success() throws Exception {
        Card activatedCard = new Card();
        activatedCard.setId(1L);
        activatedCard.setStatus(CardStatus.ACTIVE);
        activatedCard.setCardNumberMasked("**** **** **** 1234");
        activatedCard.setUser(user);  // 👈 Уже было добавлено

        when(cardService.activateCard(1L, USER_ID)).thenReturn(activatedCard);

        mockMvc.perform(post("/user/cards/1/activate")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    @DisplayName("Активация карты - карта не найдена")
    void activateCard_NotFound() throws Exception {
        when(cardService.activateCard(999L, USER_ID))
                .thenThrow(new IllegalArgumentException("Card not found"));

        mockMvc.perform(post("/user/cards/999/activate")
                        .with(user(userDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Получение баланса - успешно")
    void getCardBalance_Success() throws Exception {
        when(cardService.getCardBalance(1L, USER_ID)).thenReturn(new BigDecimal("100.00"));

        mockMvc.perform(get("/user/cards/1/balance")
                        .with(user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardId", is(1)))
                .andExpect(jsonPath("$.balance", is(100.00)));
    }

    @Test
    @DisplayName("Получение баланса - карта не найдена")
    void getCardBalance_NotFound() throws Exception {
        when(cardService.getCardBalance(999L, USER_ID))
                .thenThrow(new IllegalArgumentException("Card not found"));

        mockMvc.perform(get("/user/cards/999/balance")
                        .with(user(userDetails)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Перевод - успешно")
    void transfer_Success() throws Exception {
        when(transferService.transferBetweenOwnCards(any(TransferRequest.class), eq(USER_ID)))
                .thenReturn(transfer);

        mockMvc.perform(post("/user/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(userDetails))
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transferId", is(1)))
                .andExpect(jsonPath("$.amount", is(50.00)))
                .andExpect(jsonPath("$.message", containsString("successfully")));
    }

    @Test
    @DisplayName("Перевод - недостаточно средств")
    void transfer_InsufficientFunds() throws Exception {
        when(transferService.transferBetweenOwnCards(any(TransferRequest.class), eq(USER_ID)))
                .thenThrow(new IllegalArgumentException("Insufficient funds"));

        mockMvc.perform(post("/user/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(userDetails))
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Перевод - неверные данные (сумма <= 0)")
    void transfer_InvalidAmount() throws Exception {
        transferRequest.setAmount(BigDecimal.ZERO);

        mockMvc.perform(post("/user/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(userDetails))
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest());

        verify(transferService, never()).transferBetweenOwnCards(any(), any());
    }

    @Test
    @DisplayName("Перевод - неверные данные (null сумма)")
    void transfer_NullAmount() throws Exception {
        transferRequest.setAmount(null);

        mockMvc.perform(post("/user/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(userDetails))
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest());

        verify(transferService, never()).transferBetweenOwnCards(any(), any());
    }

    @Test
    @DisplayName("Доступ без аутентификации - ошибка")
    void accessWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/user/cards"))
                .andExpect(status().isForbidden());
    }

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers("/user/**").authenticated()
                            .anyRequest().permitAll()
                    );
            return http.build();
        }

        @Bean
        public UserDetailsService userDetailsService() {
            return username -> {
                User user = new User();
                user.setId(USER_ID);
                user.setUsername(username);
                user.setEmail("test@example.com");
                user.setPassword("password");
                user.setRole(UserRole.USER);
                user.setCreatedAt(LocalDateTime.now());
                return new CustomUserDetails(user);
            };
        }
    }
}