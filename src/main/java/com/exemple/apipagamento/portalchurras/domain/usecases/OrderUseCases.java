package com.exemple.apipagamento.portalchurras.domain.usecases;

import com.exemple.apipagamento.portalchurras.domain.entities.Order;
import com.exemple.apipagamento.portalchurras.domain.entities.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderUseCases {
    Order createOrder(String customerName, String customerEmail, String customerPhone,
                      BigDecimal total, String notes);
    Order addItemToOrder(Long orderId, Long menuItemId, Integer quantity, String observations);
    Order removeItemFromOrder(Long orderId, Long orderItemId);
    Order updateOrderItemQuantity(Long orderId, Long orderItemId, Integer newQuantity);
    Order updateOrderStatus(Long orderId, OrderStatus newStatus);
    Order updateOrderNotes(Long orderId, String notes);
    Order cancelOrder(Long orderId, String reason);

    Optional<Order> findOrderById(Long id);
    List<Order> findAllOrders();
    List<Order> findOrdersByStatus(OrderStatus status);
    List<Order> findOrdersByCustomerEmail(String customerEmail);
    List<Order> findOrdersByDateRange(LocalDateTime start, LocalDateTime end);
    List<Order> findActiveOrders();

    BigDecimal calculateOrderTotal(Long orderId);
}
