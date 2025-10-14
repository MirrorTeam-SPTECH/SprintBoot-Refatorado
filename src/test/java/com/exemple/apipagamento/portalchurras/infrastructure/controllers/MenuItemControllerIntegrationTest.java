package com.exemple.apipagamento.portalchurras.infrastructure.controllers;

import com.exemple.apipagamento.portalchurras.application.dtos.MenuItemDTO;
import com.exemple.apipagamento.portalchurras.domain.entities.MenuCategory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MenuItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void deveListarItensDoMenuAtivos() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/menu")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void deveListarItensPorCategoria() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/menu/category/HAMBURGUERES")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void deveCriarNovoItemDoMenuComPermissaoAdmin() throws Exception {
        // Given
        MenuItemDTO dto = new MenuItemDTO();
        dto.setName("Integration Test Burger " + System.currentTimeMillis());
        dto.setDescription("Test burger created via integration test");
        dto.setPrice(new BigDecimal("35.00"));
        dto.setCategory(MenuCategory.HAMBURGUERES);
        dto.setPreparationTime("20 min");

        // When & Then
        mockMvc.perform(post("/api/menu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("$.price").value(35.00));
    }

    @Test
    @WithMockUser(username = "customer@test.com", roles = "CUSTOMER")
    void deveRetornarForbiddenParaClienteCriandoItem() throws Exception {
        // Given
        MenuItemDTO dto = new MenuItemDTO();
        dto.setName("Forbidden Burger");
        dto.setDescription("Should not be created");
        dto.setPrice(new BigDecimal("25.00"));
        dto.setCategory(MenuCategory.HAMBURGUERES);
        dto.setPreparationTime("15 min");

        // When & Then
        mockMvc.perform(post("/api/menu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveBuscarItemPorId() throws Exception {
        // Given - Assumindo que existe pelo menos um item com ID 1
        // Se não existir, este teste pode falhar

        // When & Then
        mockMvc.perform(get("/api/menu/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void deveAtualizarItemDoMenu() throws Exception {
        // Given - Criar item primeiro
        MenuItemDTO createDto = new MenuItemDTO();
        createDto.setName("Update Test " + System.currentTimeMillis());
        createDto.setDescription("To be updated");
        createDto.setPrice(new BigDecimal("20.00"));
        createDto.setCategory(MenuCategory.HAMBURGUERES);
        createDto.setPreparationTime("10 min");

        String createResponse = mockMvc.perform(post("/api/menu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long createdId = objectMapper.readTree(createResponse).get("id").asLong();

        // When - Atualizar
        MenuItemDTO updateDto = new MenuItemDTO();
        updateDto.setName("Updated Burger");
        updateDto.setDescription("Updated description");
        updateDto.setPrice(new BigDecimal("30.00"));
        updateDto.setPreparationTime("25 min");

        // Then
        mockMvc.perform(put("/api/menu/" + createdId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Burger"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void deveDesativarItemDoMenu() throws Exception {
        // Given - Criar item
        MenuItemDTO dto = new MenuItemDTO();
        dto.setName("Deactivate Test " + System.currentTimeMillis());
        dto.setDescription("To be deactivated");
        dto.setPrice(new BigDecimal("15.00"));
        dto.setCategory(MenuCategory.BEBIDAS);
        dto.setPreparationTime("5 min");

        String createResponse = mockMvc.perform(post("/api/menu")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long createdId = objectMapper.readTree(createResponse).get("id").asLong();

        // When & Then - Desativar
        mockMvc.perform(delete("/api/menu/" + createdId + "/deactivate")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }
}
