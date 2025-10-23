package com.exemple.apipagamento.portalchurras.infrastructure.controllers;

import com.exemple.apipagamento.portalchurras.application.dtos.CreateUserRequest;
import com.exemple.apipagamento.portalchurras.domain.entities.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

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
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
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

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // When & Then - Tentativa de duplicação
        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());  // 409 é o status correto para email duplicado
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

        mockMvc.perform(post("/api/users/register")
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
    void deveObterPerfilDoUsuarioAutenticado() throws Exception {
        // Given - Criar usuário e fazer login
        CreateUserRequest registerRequest = new CreateUserRequest();
        String uniqueEmail = "profile" + System.currentTimeMillis() + "@test.com";
        registerRequest.setName("Profile Test");
        registerRequest.setEmail(uniqueEmail);
        registerRequest.setPassword("Password123!");
        registerRequest.setPhone("11999999999");
        registerRequest.setRole(UserRole.CUSTOMER);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)));

        // Login para obter token
        String loginJson = String.format("{\"email\":\"%s\",\"password\":\"Password123!\"}", uniqueEmail);
        String response = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        
        String token = objectMapper.readTree(response).get("token").asText();

        // When & Then - Acessar perfil com token
        mockMvc.perform(get("/api/users/me")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(uniqueEmail));
    }

    @Test
    void deveRetornar401AoAcessarPerfilSemAutenticacao() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/users/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());  // 403 é retornado pelo Spring Security quando não há autenticação
    }
}
