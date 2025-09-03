package com.exemple.apipagamento.portalchurras.infrastructure.repositories;
import com.exemple.apipagamento.portalchurras.domain.entities.Payment;
import com.exemple.apipagamento.portalchurras.domain.entities.PaymentStatus;
import com.exemple.apipagamento.portalchurras.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JpaPaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByExternalPaymentId(String externalPaymentId);

    List<Payment> findByStatus(PaymentStatus status);

    @Query("SELECT p FROM Payment p WHERE p.status IN ('PENDING', 'PROCESSING') " +
            "AND p.createdAt < :expirationTime")
    List<Payment> findExpiredPayments(@Param("expirationTime") LocalDateTime expirationTime);

    @Query("SELECT p FROM Payment p WHERE p.method = 'PIX' AND p.status = 'PENDING' " +
            "AND p.createdAt < :expirationTime")
    List<Payment> findExpiredPixPayments(@Param("expirationTime") LocalDateTime expirationTime);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = :status")
    Long countByStatus(@Param("status") PaymentStatus status);

    @Query("SELECT COUNT(p) FROM Payment p WHERE p.status = 'APPROVED' AND p.paidAt >= :startDate")
    Long countApprovedPaymentsSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.status = 'APPROVED' AND p.paidAt >= :startDate")
    Double sumApprovedAmountsSince(@Param("startDate") LocalDateTime startDate);

    boolean existsByExternalPaymentId(String externalPaymentId);

    boolean existsByOrderId(Long orderId);

    // ATUALIZADO: Para usuários registrados
    @Query("SELECT p FROM Payment p WHERE p.order.customer = :customer ORDER BY p.createdAt DESC")
    List<Payment> findByCustomerOrderByCreatedAtDesc(@Param("customer") User customer);

    // ATUALIZADO: Para usuários registrados por email
    @Query("SELECT p FROM Payment p WHERE p.order.customer.email = :customerEmail ORDER BY p.createdAt DESC")
    List<Payment> findByRegisteredCustomerEmail(@Param("customerEmail") String customerEmail);

    // NOVO: Para pedidos de convidados (dados nas observações)
    @Query("SELECT p FROM Payment p WHERE p.order.customer IS NULL " +
            "AND (p.order.notes LIKE %:emailPattern1% OR p.order.notes LIKE %:emailPattern2%) " +
            "ORDER BY p.createdAt DESC")
    List<Payment> findByGuestCustomerEmail(@Param("emailPattern1") String emailPattern1,
                                           @Param("emailPattern2") String emailPattern2);

    // NOVO: Buscar pagamentos por período para relatórios
    @Query("SELECT p FROM Payment p WHERE p.createdAt >= :startDate AND p.createdAt <= :endDate " +
            "ORDER BY p.createdAt DESC")
    List<Payment> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);
}
