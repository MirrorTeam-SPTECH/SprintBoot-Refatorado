package com.exemple.apipagamento.portalchurras.application.services;

import com.exemple.apipagamento.portalchurras.domain.entities.*;
import com.exemple.apipagamento.portalchurras.domain.ports.OrderRepository;
import com.exemple.apipagamento.portalchurras.domain.ports.UserRepository;
import com.exemple.apipagamento.portalchurras.domain.ports.MenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private OrderService orderService;

    private User testCustomer;
    private MenuItem testMenuItem;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Criar usuário com construtor correto (name, email, password, role)
        testCustomer = new User("Test Customer", "customer@test.com", "password", UserRole.CUSTOMER);
        ReflectionTestUtils.setField(testCustomer, "id", 1L);
        // Não chamar updateProfile aqui - o usuário já tem os dados necessários

        // Criar MenuItem com construtor correto
        testMenuItem = new MenuItem("X-Burger", "Hambúrguer artesanal", 
                                   new BigDecimal("25.00"), MenuCategory.HAMBURGUERES, "15 min");
        ReflectionTestUtils.setField(testMenuItem, "id", 1L);

        // Criar Order com construtor correto (User, total, notes)
        testOrder = new Order(testCustomer, BigDecimal.ZERO, "Observação");
        ReflectionTestUtils.setField(testOrder, "id", 1L);
    }

    @Test
    void deveCriarPedidoComUsuarioRegistrado() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.createOrderForUser(1L, "Observação teste");

        // Then
        assertNotNull(result);
        assertEquals(testCustomer, result.getCustomer());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void deveLancarExcecaoAoCriarPedidoComUsuarioInexistente() {
        // Given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrderForUser(999L, "Observação");
        });
    }

    @Test
    void deveCriarPedidoConvidadoComSucesso() {
        // Given
        when(userRepository.findByEmail("guest@test.com")).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.createOrder("Guest Name", "guest@test.com", 
                                                "11999999999", BigDecimal.ZERO, "Observação");

        // Then
        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void deveAdicionarItemAoPedido() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testMenuItem));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.addItemToOrder(1L, 1L, 2, "Sem cebola");

        // Then
        assertNotNull(result);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void deveLancarExcecaoAoAdicionarItemInexistente() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(menuItemRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then - O serviço lança RuntimeException encapsulando IllegalArgumentException
        assertThrows(RuntimeException.class, () -> {
            orderService.addItemToOrder(1L, 999L, 2, "");
        });
    }

    @Test
    void deveAtualizarStatusDoPedido() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When - PENDING -> CONFIRMED é uma transição válida
        Order result = orderService.updateOrderStatus(1L, OrderStatus.CONFIRMED);

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void deveCancelarPedido() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // When
        Order result = orderService.cancelOrder(1L, "Cliente solicitou cancelamento");

        // Then
        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void deveBuscarPedidoPorId() {
        // Given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // When
        Optional<Order> result = orderService.findOrderById(1L);

        // Then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    void deveBuscarPedidosAtivos() {
        // Given
        List<Order> activeOrders = List.of(testOrder);
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(activeOrders);
        when(orderRepository.findByStatus(OrderStatus.CONFIRMED)).thenReturn(List.of());
        when(orderRepository.findByStatus(OrderStatus.IN_PREPARATION)).thenReturn(List.of());
        when(orderRepository.findByStatus(OrderStatus.READY)).thenReturn(List.of());

        // When
        List<Order> result = orderService.findActiveOrders();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void deveBuscarPedidosPorStatus() {
        // Given
        List<Order> orders = List.of(testOrder);
        when(orderRepository.findByStatus(OrderStatus.PENDING)).thenReturn(orders);

        // When
        List<Order> result = orderService.findOrdersByStatus(OrderStatus.PENDING);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(OrderStatus.PENDING, result.get(0).getStatus());
    }

    @Test
    void deveBuscarPedidosDoCliente() {
        // Given
        List<Order> customerOrders = List.of(testOrder);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testCustomer));
        when(orderRepository.findByCustomer(testCustomer)).thenReturn(customerOrders);

        // When
        List<Order> result = orderService.findOrdersByCustomer(1L);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCustomer, result.get(0).getCustomer());
    }
}
