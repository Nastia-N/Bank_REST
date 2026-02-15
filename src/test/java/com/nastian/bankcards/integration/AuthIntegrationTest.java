package com.nastian.bankcards.integration;

import com.nastian.bankcards.dto.AuthRequest;
import com.nastian.bankcards.dto.LoginRequest;
import com.nastian.bankcards.entity.UserRole;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@AutoConfigureMockMvc
class AuthIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Полный цикл: регистрация → логин → проверка JWT")
    void fullAuthCycle_Success() throws Exception {
        String uniqueId = String.valueOf(System.currentTimeMillis());
        String username = "user" + uniqueId;
        String email = "user" + uniqueId + "@test.com";
        String password = "password123";

        AuthRequest registerRequest = new AuthRequest();
        registerRequest.setUsername(username);
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        registerRequest.setRole(UserRole.USER);

        MvcResult registerResult = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value(username))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.role").value("USER"))
                .andReturn();

        JsonNode registerResponse = objectMapper.readTree(registerResult.getResponse().getContentAsString());
        String jwtToken = registerResponse.get("token").asText();
        Long userId = registerResponse.get("id").asLong();

        assertThat(jwtToken).isNotBlank();
        assertThat(userId).isPositive();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(username);
        loginRequest.setPassword(password);

        MvcResult loginResult = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.id").value(userId))
                .andExpect(jsonPath("$.username").value(username))
                .andReturn();

        JsonNode loginResponse = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String newToken = loginResponse.get("token").asText();

        assertThat(newToken).isNotBlank();
    }

    @Test
    @DisplayName("Ошибка при регистрации с существующим username")
    void register_DuplicateUsername_ShouldFail() throws Exception {
        String username = "duplicate" + System.currentTimeMillis();
        String email = "duplicate@test.com";
        String password = "password123";

        AuthRequest request1 = new AuthRequest();
        request1.setUsername(username);
        request1.setEmail(email);
        request1.setPassword(password);
        request1.setRole(UserRole.USER);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1)))
                .andExpect(status().isOk());

        AuthRequest request2 = new AuthRequest();
        request2.setUsername(username);
        request2.setEmail("other@test.com");
        request2.setPassword(password);
        request2.setRole(UserRole.USER);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Username already exists")));
    }
}
