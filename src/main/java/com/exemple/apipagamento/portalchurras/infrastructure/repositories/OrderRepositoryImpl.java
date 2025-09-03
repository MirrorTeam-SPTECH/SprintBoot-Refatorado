package com.exemple.apipagamento.portalchurras.infrastructure.repositories;

import com.exemple.apipagamento.portalchurras.domain.entities.Order;
import com.exemple.apipagamento.portalchurras.domain.entities.OrderStatus;
import com.exemple.apipagamento.portalchurras.domain.entities.User;
import com.exemple.apipagamento.portalchurras.domain.ports.OrderRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class OrderRepositoryImpl implements OrderRepository {

    private final JpaOrderRepository jpaRepository;

    public OrderRepositoryImpl(JpaOrderRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Order save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order não pode ser nulo");
        }
        return jpaRepository.save(order);
    }

    @Override
    public Optional<Order> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return jpaRepository.findById(id);
    }

    @Override
    public List<Order> findAll() {
        return jpaRepository.findAll();
    }

    @Override
    public List<Order> findByStatus(OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status não pode ser nulo");
        }
        return jpaRepository.findByStatus(status);
    }

    @Override
    public List<Order> findByCustomer(User customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer não pode ser nulo");
        }
        return jpaRepository.findByCustomerOrderByCreatedAtDesc(customer);
    }

    @Override
    public List<Order> findByCustomerEmail(String customerEmail) {
        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Customer email não pode estar vazio");
        }
        return jpaRepository.findByCustomerEmail(customerEmail.trim());
    }

    @Override
    public List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Datas de início e fim são obrigatórias");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Data de início não pode ser posterior à data de fim");
        }
        return jpaRepository.findByCreatedAtBetween(start, end);
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        if (!jpaRepository.existsById(id)) {
            throw new IllegalArgumentException("Order com ID " + id + " não encontrado");
        }
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }
        return jpaRepository.existsById(id);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }
}
