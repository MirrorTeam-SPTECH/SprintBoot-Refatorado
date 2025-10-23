package com.exemple.apipagamento.portalchurras.infrastructure.controllers;

import com.exemple.apipagamento.portalchurras.application.dtos.MenuItemDTO;
import com.exemple.apipagamento.portalchurras.application.services.UserService;
import com.exemple.apipagamento.portalchurras.domain.entities.MenuCategory;
import com.exemple.apipagamento.portalchurras.domain.entities.User;
import com.exemple.apipagamento.portalchurras.domain.entities.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
    
    @Autowired
    private UserService userService;
    
    private String adminToken;
    private String customerToken;

    @BeforeEach
    void setUp() throws Exception {
        // Criar usuário admin diretamente via service
        String adminEmail = "admin" + System.currentTimeMillis() + "@test.com";
        User admin = userService.createUser("Admin Test", adminEmail, "Admin123!", UserRole.ADMIN);

        String adminLoginJson = String.format("{\"email\":\"%s\",\"password\":\"Admin123!\"}", adminEmail);
        String adminResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(adminLoginJson))
                .andReturn().getResponse().getContentAsString();
        adminToken = objectMapper.readTree(adminResponse).get("token").asText();

        // Criar usuário customer diretamente via service
        String customerEmail = "customer" + System.currentTimeMillis() + "@test.com";
        User customer = userService.createUser("Customer Test", customerEmail, "Customer123!", UserRole.CUSTOMER);

        String customerLoginJson = String.format("{\"email\":\"%s\",\"password\":\"Customer123!\"}", customerEmail);
        String customerResponse = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(customerLoginJson))
                .andReturn().getResponse().getContentAsString();
        customerToken = objectMapper.readTree(customerResponse).get("token").asText();
    }

    @Test
    void deveListarItensDoMenuAtivos() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/menu-items")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void deveListarItensPorCategoria() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/menu-items/category/HAMBURGUERES")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void deveCriarNovoItemDoMenuComPermissaoAdmin() throws Exception {
        // Given
        MenuItemDTO dto = new MenuItemDTO();
        dto.setName("Integration Test Burger " + System.currentTimeMillis());
        dto.setDescription("Test burger created via integration test");
        dto.setPrice(new BigDecimal("35.00"));
        dto.setCategory(MenuCategory.HAMBURGUERES);
        dto.setPreparationTime("20 min");

        // When & Then
        mockMvc.perform(post("/api/menu-items")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(dto.getName()))
                .andExpect(jsonPath("$.price").value(35.00));
    }

    @Test
    void deveRetornarForbiddenParaClienteCriandoItem() throws Exception {
        // Given
        MenuItemDTO dto = new MenuItemDTO();
        dto.setName("Forbidden Burger");
        dto.setDescription("Should not be created");
        dto.setPrice(new BigDecimal("25.00"));
        dto.setCategory(MenuCategory.HAMBURGUERES);
        dto.setPreparationTime("15 min");

        // When & Then
        mockMvc.perform(post("/api/menu-items")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveBuscarItemPorId() throws Exception {
        // Given - Criar um item primeiro
        MenuItemDTO dto = new MenuItemDTO();
        dto.setName("Search Test " + System.currentTimeMillis());
        dto.setDescription("Item to be searched");
        dto.setPrice(new BigDecimal("20.00"));
        dto.setCategory(MenuCategory.HAMBURGUERES);
        dto.setPreparationTime("15 min");

        String createResponse = mockMvc.perform(post("/api/menu-items")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();

        Long createdId = objectMapper.readTree(createResponse).get("id").asLong();

        // When & Then
        mockMvc.perform(get("/api/menu-items/" + createdId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    void deveAtualizarItemDoMenu() throws Exception {
        // Given - Criar item primeiro
        MenuItemDTO createDto = new MenuItemDTO();
        createDto.setName("Update Test " + System.currentTimeMillis());
        createDto.setDescription("To be updated");
        createDto.setPrice(new BigDecimal("20.00"));
        createDto.setCategory(MenuCategory.HAMBURGUERES);
        createDto.setPreparationTime("10 min");

        String createResponse = mockMvc.perform(post("/api/menu-items")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isCreated())
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
        mockMvc.perform(put("/api/menu-items/" + createdId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Burger"));
    }

    @Test
    void deveDesativarItemDoMenu() throws Exception {
        // Given - Criar item
        MenuItemDTO dto = new MenuItemDTO();
        dto.setName("Deactivate Test " + System.currentTimeMillis());
        dto.setDescription("To be deactivated");
        dto.setPrice(new BigDecimal("15.00"));
        dto.setCategory(MenuCategory.BEBIDAS);
        dto.setPreparationTime("5 min");

        String createResponse = mockMvc.perform(post("/api/menu-items")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long createdId = objectMapper.readTree(createResponse).get("id").asLong();

        // When & Then - Desativar
        mockMvc.perform(delete("/api/menu-items/" + createdId + "/deactivate")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }
}
