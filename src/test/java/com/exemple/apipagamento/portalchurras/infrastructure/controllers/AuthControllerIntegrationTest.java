package com.exemple.apipagamento.portalchurras.infrastructure.controllers;

import com.exemple.apipagamento.portalchurras.application.dtos.CreateUserRequest;
import com.exemple.apipagamento.portalchurras.domain.entities.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveRegistrarNovoUsuarioComSucesso() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Integration Test User");
        request.setEmail("integration" + System.currentTimeMillis() + "@test.com");
        request.setPassword("Password123!");
        request.setPhone("11999999999");
        request.setRole(UserRole.CUSTOMER);

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(request.getEmail()))
                .andExpect(jsonPath("$.name").value(request.getName()));
    }

    @Test
    void deveRetornarErroAoRegistrarEmailDuplicado() throws Exception {
        // Given - Primeiro registro
        CreateUserRequest request = new CreateUserRequest();
        String uniqueEmail = "duplicate" + System.currentTimeMillis() + "@test.com";
        request.setName("Test User");
        request.setEmail(uniqueEmail);
        request.setPassword("Password123!");
        request.setPhone("11999999999");
        request.setRole(UserRole.CUSTOMER);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // When & Then - Tentativa de duplicação
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveFazerLoginComCredenciaisValidas() throws Exception {
        // Given - Criar usuário primeiro
        CreateUserRequest registerRequest = new CreateUserRequest();
        String uniqueEmail = "login" + System.currentTimeMillis() + "@test.com";
        registerRequest.setName("Login Test");
        registerRequest.setEmail(uniqueEmail);
        registerRequest.setPassword("Password123!");
        registerRequest.setPhone("11999999999");
        registerRequest.setRole(UserRole.CUSTOMER);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // When & Then - Login
        String loginJson = String.format("{\"email\":\"%s\",\"password\":\"Password123!\"}", uniqueEmail);
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void deveRetornarErroComCredenciaisInvalidas() throws Exception {
        // When & Then
        String loginJson = "{\"email\":\"nonexistent@test.com\",\"password\":\"wrongpassword\"}";
        
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@test.com", roles = "CUSTOMER")
    void deveObterPerfilDoUsuarioAutenticado() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deveRetornar401AoAcessarPerfilSemAutenticacao() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/auth/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
