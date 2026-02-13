package com.example.bankcards.controller;

import com.example.bankcards.dto.CardRequest;
import com.example.bankcards.dto.UpdateRoleRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.entity.UserRole;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.security.JwtTokenProvider;
import com.example.bankcards.service.AdminService;
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
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(AdminControllerTest.TestSecurityConfig.class)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminService adminService;

    @MockBean
    private JwtTokenProvider tokenProvider;

    private User admin;
    private User regularUser;
    private CustomUserDetails adminDetails;
    private Card card;
    private CardRequest cardRequest;
    private UpdateRoleRequest updateRoleRequest;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        admin = new User();
        admin.setId(1L);
        admin.setUsername("admin");
        admin.setEmail("admin@test.com");
        admin.setPassword("password");
        admin.setRole(UserRole.ADMIN);
        admin.setCreatedAt(LocalDateTime.now());

        adminDetails = new CustomUserDetails(admin);

        regularUser = new User();
        regularUser.setId(2L);
        regularUser.setUsername("user");
        regularUser.setEmail("user@test.com");
        regularUser.setPassword("password");
        regularUser.setRole(UserRole.USER);
        regularUser.setCreatedAt(LocalDateTime.now());

        card = new Card();
        card.setId(1L);
        card.setUser(regularUser);
        card.setCardHolderName("Test User");
        card.setExpirationDate(LocalDate.now().plusYears(1));
        card.setStatus("ACTIVE");
        card.setBalance(new BigDecimal("100.00"));
        card.setCardNumberMasked("**** **** **** 1234");

        cardRequest = new CardRequest();
        cardRequest.setCardHolderName("New Card");
        cardRequest.setExpirationDate(LocalDate.now().plusYears(1));

        updateRoleRequest = new UpdateRoleRequest();
        updateRoleRequest.setRole(UserRole.ADMIN);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("Получение всех пользователей - успешно")
    void getAllUsers_Success() throws Exception {
        PageImpl<User> page = new PageImpl<>(List.of(regularUser, admin), pageable, 2);
        when(adminService.getAllUsers(isNull(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/admin/users")
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", is(2)))
                .andExpect(jsonPath("$.content[0].username", is("user")))
                .andExpect(jsonPath("$.content[0].email", is("user@test.com")))
                .andExpect(jsonPath("$.content[0].role", is("USER")))
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    @DisplayName("Поиск пользователей - успешно")
    void getAllUsers_WithSearch_Success() throws Exception {
        PageImpl<User> page = new PageImpl<>(List.of(regularUser), pageable, 1);
        when(adminService.getAllUsers(eq("user"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/admin/users")
                        .param("search", "user")
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username", is("user")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("Получение пользователя по ID - успешно")
    void getUserById_Success() throws Exception {
        when(adminService.getUserById(2L)).thenReturn(regularUser);

        mockMvc.perform(get("/admin/users/2")
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.username", is("user")))
                .andExpect(jsonPath("$.email", is("user@test.com")))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    @DisplayName("Получение пользователя по ID - не найден")
    void getUserById_NotFound() throws Exception {
        when(adminService.getUserById(999L))
                .thenThrow(new IllegalArgumentException("User not found with id: 999"));

        mockMvc.perform(get("/admin/users/999")
                        .with(user(adminDetails)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("User not found")));
    }

    @Test
    @DisplayName("Изменение роли пользователя - успешно")
    void updateUserRole_Success() throws Exception {
        regularUser.setRole(UserRole.ADMIN);
        when(adminService.updateUserRole(2L, UserRole.ADMIN)).thenReturn(regularUser);

        mockMvc.perform(put("/admin/users/2/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(adminDetails))
                        .content(objectMapper.writeValueAsString(updateRoleRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(2)))
                .andExpect(jsonPath("$.username", is("user")))
                .andExpect(jsonPath("$.role", is("ADMIN")));
    }

    @Test
    @DisplayName("Изменение роли пользователя - неверная роль")
    void updateUserRole_InvalidRole() throws Exception {
        updateRoleRequest.setRole(null);

        mockMvc.perform(put("/admin/users/2/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(adminDetails))
                        .content(objectMapper.writeValueAsString(updateRoleRequest)))
                .andExpect(status().isBadRequest());  // Должно быть 400, а не 500

        verify(adminService, never()).updateUserRole(any(), any());
    }

    @Test
    @DisplayName("Изменение роли пользователя - пользователь не найден")
    void updateUserRole_UserNotFound() throws Exception {
        when(adminService.updateUserRole(999L, UserRole.ADMIN))
                .thenThrow(new IllegalArgumentException("User not found with id: 999"));

        mockMvc.perform(put("/admin/users/999/role")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(adminDetails))
                        .content(objectMapper.writeValueAsString(updateRoleRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("User not found")));
    }

    @Test
    @DisplayName("Удаление пользователя - успешно")
    void deleteUser_Success() throws Exception {
        doNothing().when(adminService).deleteUser(2L);

        mockMvc.perform(delete("/admin/users/2")
                        .with(user(adminDetails)))
                .andExpect(status().isNoContent());

        verify(adminService).deleteUser(2L);
    }

    @Test
    @DisplayName("Удаление пользователя - не найден")
    void deleteUser_NotFound() throws Exception {
        doThrow(new IllegalArgumentException("User not found with id: 999"))
                .when(adminService).deleteUser(999L);

        mockMvc.perform(delete("/admin/users/999")
                        .with(user(adminDetails)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("User not found")));
    }

    @Test
    @DisplayName("Получение всех карт - успешно")
    void getAllCards_Success() throws Exception {
        PageImpl<Card> page = new PageImpl<>(List.of(card), pageable, 1);
        when(adminService.getAllCards(isNull(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/admin/cards")
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.content[0].cardNumberMasked", is("**** **** **** 1234")))
                .andExpect(jsonPath("$.content[0].cardHolderName", is("Test User")))
                .andExpect(jsonPath("$.content[0].status", is("ACTIVE")))
                .andExpect(jsonPath("$.content[0].balance", is(100.00)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("Поиск карт - успешно")
    void getAllCards_WithSearch_Success() throws Exception {
        PageImpl<Card> page = new PageImpl<>(List.of(card), pageable, 1);
        when(adminService.getAllCards(eq("1234"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/admin/cards")
                        .param("search", "1234")
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("Получение карт пользователя - успешно")
    void getUserCards_Success() throws Exception {
        PageImpl<Card> page = new PageImpl<>(List.of(card), pageable, 1);
        when(adminService.getUserCards(eq(2L), isNull(), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/admin/users/2/cards")
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", is(1)))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("Получение карт пользователя с поиском - успешно")
    void getUserCards_WithSearch_Success() throws Exception {
        PageImpl<Card> page = new PageImpl<>(List.of(card), pageable, 1);
        when(adminService.getUserCards(eq(2L), eq("1234"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/admin/users/2/cards")
                        .param("search", "1234")
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id", is(1)));
    }

    @Test
    @DisplayName("Создание карты для пользователя - успешно")
    void createCardForUser_Success() throws Exception {
        when(adminService.createCardForUser(eq(2L), any(CardRequest.class))).thenReturn(card);

        mockMvc.perform(post("/admin/users/2/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(adminDetails))
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.cardHolderName", is("Test User")))
                .andExpect(jsonPath("$.status", is("ACTIVE")));
    }

    @Test
    @DisplayName("Создание карты для пользователя - пользователь не найден")
    void createCardForUser_UserNotFound() throws Exception {
        when(adminService.createCardForUser(eq(999L), any(CardRequest.class)))
                .thenThrow(new IllegalArgumentException("User not found with id: 999"));

        mockMvc.perform(post("/admin/users/999/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .with(user(adminDetails))
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("User not found")));
    }

    @Test
    @DisplayName("Изменение статуса карты - успешно")
    void updateCardStatus_Success() throws Exception {
        card.setStatus("BLOCKED");
        when(adminService.updateCardStatus(1L, "BLOCKED")).thenReturn(card);

        mockMvc.perform(put("/admin/cards/1/status")
                        .param("status", "BLOCKED")
                        .with(user(adminDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.status", is("BLOCKED")));
    }

    @Test
    @DisplayName("Изменение статуса карты - неверный статус")
    void updateCardStatus_InvalidStatus() throws Exception {
        when(adminService.updateCardStatus(1L, "INVALID"))
                .thenThrow(new IllegalArgumentException("Invalid status. Allowed: ACTIVE, BLOCKED"));

        mockMvc.perform(put("/admin/cards/1/status")
                        .param("status", "INVALID")
                        .with(user(adminDetails)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Invalid status")));
    }

    @Test
    @DisplayName("Изменение статуса карты - карта не найдена")
    void updateCardStatus_CardNotFound() throws Exception {
        when(adminService.updateCardStatus(999L, "BLOCKED"))
                .thenThrow(new IllegalArgumentException("Card not found with id: 999"));

        mockMvc.perform(put("/admin/cards/999/status")
                        .param("status", "BLOCKED")
                        .with(user(adminDetails)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Card not found")));
    }

    @Test
    @DisplayName("Удаление карты - успешно")
    void deleteCard_Success() throws Exception {
        doNothing().when(adminService).deleteCard(1L);

        mockMvc.perform(delete("/admin/cards/1")
                        .with(user(adminDetails)))
                .andExpect(status().isNoContent());

        verify(adminService).deleteCard(1L);
    }

    @Test
    @DisplayName("Удаление карты - не найдена")
    void deleteCard_NotFound() throws Exception {
        doThrow(new IllegalArgumentException("Card not found with id: 999"))
                .when(adminService).deleteCard(999L);

        mockMvc.perform(delete("/admin/cards/999")
                        .with(user(adminDetails)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Card not found")));
    }

    @TestConfiguration
    @EnableWebSecurity
    static class TestSecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(auth -> auth
                            .anyRequest().permitAll()
                    );
            return http.build();
        }

        @Bean
        public UserDetailsService userDetailsService() {
            return username -> {
                User admin = new User();
                admin.setId(1L);
                admin.setUsername("admin");
                admin.setEmail("admin@test.com");
                admin.setPassword("password");
                admin.setRole(UserRole.ADMIN);
                admin.setCreatedAt(LocalDateTime.now());
                return new CustomUserDetails(admin);
            };
        }
    }
}