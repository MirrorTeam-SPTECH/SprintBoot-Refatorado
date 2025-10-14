package com.exemple.apipagamento.portalchurras.infrastructure.controllers;

import com.exemple.apipagamento.portalchurras.application.dtos.CreateOrderRequest;
import com.exemple.apipagamento.portalchurras.domain.entities.*;
import com.exemple.apipagamento.portalchurras.domain.ports.UserRepository;
import com.exemple.apipagamento.portalchurras.domain.ports.OrderRepository;
import com.exemple.apipagamento.portalchurras.infrastructure.repositories.JpaMenuItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
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
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JpaMenuItemRepository menuItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    private User testCustomer;
    private MenuItem testMenuItem;

    @BeforeEach
    void setUp() {
        // Criar usuário de teste
        if (!userRepository.existsByEmail("ordertest@test.com")) {
            testCustomer = new User("Order Test", "ordertest@test.com", "password", UserRole.CUSTOMER);
            testCustomer = userRepository.save(testCustomer);
        } else {
            testCustomer = userRepository.findByEmail("ordertest@test.com").orElseThrow();
        }

        // Criar item do menu de teste com construtor correto
        testMenuItem = new MenuItem("Test Burger", "Test burger for integration",
                                   new BigDecimal("25.00"), MenuCategory.HAMBURGUERES, "15 min");
        testMenuItem = menuItemRepository.save(testMenuItem);
    }

    @Test
    @WithMockUser(username = "ordertest@test.com", roles = "CUSTOMER")
    void deveCriarPedidoComSucesso() throws Exception {
        // Given
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerId(testCustomer.getId());
        request.setNotes("Teste de integração");

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void deveListarTodosPedidos() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/orders")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "ordertest@test.com", roles = "CUSTOMER")
    void deveBuscarPedidoPorId() throws Exception {
        // Given - Criar pedido primeiro
        Order order = new Order(testCustomer, BigDecimal.ZERO, "Teste");
        order = orderRepository.save(order);

        // When & Then
        mockMvc.perform(get("/api/orders/" + order.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()));
    }

    @Test
    void deveRetornar401SemAutenticacao() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/orders")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "ordertest@test.com", roles = "CUSTOMER")
    void deveListarPedidosDoCliente() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/orders/customer/" + testCustomer.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void deveAtualizarStatusDoPedido() throws Exception {
        // Given
        Order order = new Order(testCustomer, BigDecimal.ZERO, "Teste");
        order = orderRepository.save(order);

        // When & Then
        mockMvc.perform(patch("/api/orders/" + order.getId() + "/status")
                .param("status", "IN_PREPARATION")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PREPARATION"));
    }
}
