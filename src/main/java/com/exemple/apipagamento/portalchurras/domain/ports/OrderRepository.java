package com.exemple.apipagamento.portalchurras.domain.ports;

import com.exemple.apipagamento.portalchurras.domain.entities.Order;
import com.exemple.apipagamento.portalchurras.domain.entities.OrderStatus;
import com.exemple.apipagamento.portalchurras.domain.entities.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long id);

    List<Order> findAll();

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCustomerEmail(String customerEmail); // Para pedidos de convidados

    List<Order> findByCustomer(User customer); // ADICIONADO: Para pedidos de usuários registrados

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    void deleteById(Long id);

    boolean existsById(Long id);

    long count();
}