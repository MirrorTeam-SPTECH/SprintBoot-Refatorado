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
    private User testAdmin;
    private MenuItem testMenuItem;

    @BeforeEach
    void setUp() {
        // Limpar dados anteriores
        orderRepository.deleteAll();
        menuItemRepository.deleteAll();
        
        // Criar usuário customer de teste
        if (!userRepository.existsByEmail("ordertest@test.com")) {
            testCustomer = new User("Order Test", "ordertest@test.com", "password", UserRole.CUSTOMER);
            testCustomer = userRepository.save(testCustomer);
        } else {
            testCustomer = userRepository.findByEmail("ordertest@test.com").orElseThrow();
        }
        
        // Criar usuário admin de teste
        if (!userRepository.existsByEmail("admin@test.com")) {
            testAdmin = new User("Admin Test", "admin@test.com", "password", UserRole.ADMIN);
            testAdmin = userRepository.save(testAdmin);
        } else {
            testAdmin = userRepository.findByEmail("admin@test.com").orElseThrow();
        }

        // Criar item do menu de teste com construtor correto
        testMenuItem = new MenuItem("Test Burger", "Test burger for integration",
                                   new BigDecimal("25.00"), MenuCategory.HAMBURGUERES, "15 min");
        testMenuItem = menuItemRepository.save(testMenuItem);
    }

    @Test
    @WithMockUser(username = "ordertest@test.com", roles = "CUSTOMER")
    void deveCriarPedidoComSucesso() throws Exception {
        // Given - Criar pedido como guest (controller atual não usa customerId)
        CreateOrderRequest request = new CreateOrderRequest();
        request.setCustomerName("Order Test Customer");
        request.setCustomerEmail("ordertest@test.com");
        request.setCustomerPhone("11999999999");
        request.setTotal(new BigDecimal("50.00"));
        request.setNotes("Teste de integração");

        // When & Then
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void deveListarTodosPedidos() throws Exception {
        // When & Then - O endpoint retorna um objeto com 'content' (array), não um array direto
        mockMvc.perform(get("/api/orders")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").exists());
    }

    @Test
    @WithMockUser(username = "ordertest@test.com", roles = "CUSTOMER")
    void deveBuscarPedidoPorId() throws Exception {
        // Given - Criar pedido primeiro
        Order order = new Order(testCustomer, new BigDecimal("0.01"), "Teste");
        order = orderRepository.save(order);

        // When & Then
        mockMvc.perform(get("/api/orders/" + order.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(order.getId()));
    }

    @Test
    void deveRetornar401SemAutenticacao() throws Exception {
        // When & Then - Spring Security retorna 403 (Forbidden) ao invés de 401 quando não autenticado
        // Isso é comportamento padrão do Spring Security 6.x
        mockMvc.perform(get("/api/orders")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());  // 403, não 401
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void deveListarPedidosDoCliente() throws Exception {
        // When & Then - O endpoint usa email, não ID do cliente
        // Requer role ADMIN/EMPLOYEE conforme SecurityConfig
        // Precisa criar um pedido primeiro para garantir que há dados
        Order order = new Order(testCustomer, new BigDecimal("0.01"), "Teste listagem");
        orderRepository.save(order);
        
        mockMvc.perform(get("/api/orders/customer/" + testCustomer.getEmail())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "admin@test.com", roles = "ADMIN")
    void deveAtualizarStatusDoPedido() throws Exception {
        // Given
        Order order = new Order(testCustomer, new BigDecimal("0.01"), "Teste");
        order = orderRepository.save(order);

        // When & Then - Transição válida: PENDING -> CONFIRMED
        mockMvc.perform(patch("/api/orders/" + order.getId() + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"CONFIRMED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }
}
