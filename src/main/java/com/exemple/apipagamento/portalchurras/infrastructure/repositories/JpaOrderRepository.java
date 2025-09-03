package com.exemple.apipagamento.portalchurras.infrastructure.repositories;

import com.exemple.apipagamento.portalchurras.domain.entities.Order;
import com.exemple.apipagamento.portalchurras.domain.entities.OrderStatus;
import com.exemple.apipagamento.portalchurras.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JpaOrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByStatus(OrderStatus status);

    List<Order> findByCustomerEmail(String customerEmail);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT o FROM Order o WHERE o.status IN :statuses ORDER BY o.createdAt DESC")
    List<Order> findByStatusInOrderByCreatedAtDesc(@Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT o FROM Order o WHERE o.customer = :customer ORDER BY o.createdAt DESC")
    List<Order> findByCustomerOrderByCreatedAtDesc(@Param("customer") User customer);

    @Query("SELECT o FROM Order o WHERE o.status IN ('PENDING', 'CONFIRMED', 'IN_PREPARATION', 'READY') ORDER BY o.createdAt ASC")
    List<Order> findActiveOrdersOrderByCreatedAt();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(@Param("status") OrderStatus status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createdAt >= :startDate")
    Long countOrdersSinceDate(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COALESCE(SUM(o.total), 0) FROM Order o WHERE o.status != 'CANCELLED' AND o.createdAt >= :startDate")
    Double sumTotalSinceDate(@Param("startDate") LocalDateTime startDate);

    boolean existsByCustomerEmailAndStatus(String customerEmail, OrderStatus status);
}
