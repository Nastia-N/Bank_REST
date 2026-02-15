package com.nastian.bankcards.integration;

import com.nastian.bankcards.dto.AuthRequest;
import com.nastian.bankcards.dto.CardRequest;
import com.nastian.bankcards.entity.UserRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
class CardFlowIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        String username = "carduser" + uniqueId;

        AuthRequest registerRequest = new AuthRequest();
        registerRequest.setUsername(username);
        registerRequest.setEmail(username + "@test.com");
        registerRequest.setPassword("password123");
        registerRequest.setRole(UserRole.USER);

        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode registerResponse = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        jwtToken = registerResponse.get("token").asText();
        Long userId = registerResponse.get("id").asLong();
    }

    @Test
    @DisplayName("Полный цикл работы с картой: создание → проверка → блокировка → активация")
    void fullCardLifecycle_Success() throws Exception {
        CardRequest cardRequest = new CardRequest();
        cardRequest.setCardHolderName("Test User");
        cardRequest.setExpirationDate(LocalDate.now().plusYears(2));

        MvcResult createResult = mockMvc.perform(post("/user/cards")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.cardNumberMasked").value(org.hamcrest.Matchers.matchesPattern("\\*{4} \\*{4} \\*{4} \\d{4}")))
                .andExpect(jsonPath("$.cardHolderName").value("Test User"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(0))
                .andReturn();

        JsonNode cardResponse = objectMapper.readTree(createResult.getResponse().getContentAsString());
        Long cardId = cardResponse.get("id").asLong();

        mockMvc.perform(get("/user/cards")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(cardId))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/user/cards/" + cardId + "/balance")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardId").value(cardId))
                .andExpect(jsonPath("$.balance").value(0));

        mockMvc.perform(post("/user/cards/" + cardId + "/block")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId))
                .andExpect(jsonPath("$.status").value("BLOCKED"));

        mockMvc.perform(get("/user/cards/" + cardId + "/balance")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk()); // баланс всё ещё можно смотреть

        mockMvc.perform(post("/user/cards/" + cardId + "/activate")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(cardId))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Ошибка при создании карты с истекшим сроком")
    void createCard_WithExpiredDate_ShouldFail() throws Exception {
        CardRequest cardRequest = new CardRequest();
        cardRequest.setCardHolderName("Test User");
        cardRequest.setExpirationDate(LocalDate.now().minusDays(1)); // просрочена

        mockMvc.perform(post("/user/cards")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isBadRequest());
    }
}