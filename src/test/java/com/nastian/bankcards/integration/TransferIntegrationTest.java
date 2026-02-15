package com.nastian.bankcards.integration;

import com.nastian.bankcards.dto.AuthRequest;
import com.nastian.bankcards.dto.CardRequest;
import com.nastian.bankcards.dto.TransferRequest;
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

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
class TransferIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;
    private Long fromCardId;
    private Long toCardId;

    @BeforeEach
    void setUp() throws Exception {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        String username = "transfer" + uniqueId;

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

        CardRequest cardRequest = new CardRequest();
        cardRequest.setCardHolderName("Test User");
        cardRequest.setExpirationDate(LocalDate.now().plusYears(2));

        MvcResult card1Result = mockMvc.perform(post("/user/cards")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode card1Response = objectMapper.readTree(card1Result.getResponse().getContentAsString());
        fromCardId = card1Response.get("id").asLong();

        MvcResult card2Result = mockMvc.perform(post("/user/cards")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardRequest)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode card2Response = objectMapper.readTree(card2Result.getResponse().getContentAsString());
        toCardId = card2Response.get("id").asLong();
    }

    @Test
    @DisplayName("Успешный перевод между картами")
    void transferBetweenCards_Success() throws Exception {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromCardId(fromCardId);
        transferRequest.setToCardId(toCardId);
        transferRequest.setAmount(new BigDecimal("50.00"));

        mockMvc.perform(post("/user/transfers")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Ошибка при переводе на ту же карту")
    void transferToSameCard_ShouldFail() throws Exception {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromCardId(fromCardId);
        transferRequest.setToCardId(fromCardId); // та же карта
        transferRequest.setAmount(new BigDecimal("50.00"));

        mockMvc.perform(post("/user/transfers")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Cannot transfer money to the same card"));
    }

    @Test
    @DisplayName("Ошибка при переводе с неверной суммой")
    void transferWithInvalidAmount_ShouldFail() throws Exception {
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setFromCardId(fromCardId);
        transferRequest.setToCardId(toCardId);
        transferRequest.setAmount(new BigDecimal("-10.00"));

        mockMvc.perform(post("/user/transfers")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(transferRequest)))
                .andExpect(status().isBadRequest());
    }
}