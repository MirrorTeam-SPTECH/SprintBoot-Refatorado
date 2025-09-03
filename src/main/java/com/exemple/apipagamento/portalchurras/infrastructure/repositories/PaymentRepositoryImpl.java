package com.exemple.apipagamento.portalchurras.infrastructure.repositories;

import com.exemple.apipagamento.portalchurras.domain.entities.Payment;
import com.exemple.apipagamento.portalchurras.domain.entities.PaymentStatus;
import com.exemple.apipagamento.portalchurras.domain.entities.User;
import com.exemple.apipagamento.portalchurras.domain.ports.PaymentRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class PaymentRepositoryImpl implements PaymentRepository {

    private final JpaPaymentRepository jpaRepository;

    // Pagamentos expiram após 30 minutos por padrão
    private static final int EXPIRATION_MINUTES = 30;

    public PaymentRepositoryImpl(JpaPaymentRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Payment save(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment não pode ser nulo");
        }
        return jpaRepository.save(payment);
    }

    @Override
    public Optional<Payment> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<Payment> findByOrderId(Long orderId) {
        if (orderId == null) {
            return Optional.empty();
        }
        return jpaRepository.findByOrderId(orderId);
    }

    @Override
    public Optional<Payment> findByExternalPaymentId(String externalPaymentId) {
        if (externalPaymentId == null || externalPaymentId.trim().isEmpty()) {
            return Optional.empty();
        }
        return jpaRepository.findByExternalPaymentId(externalPaymentId.trim());
    }

    @Override
    public List<Payment> findByStatus(PaymentStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Status não pode ser nulo");
        }
        return jpaRepository.findByStatus(status);
    }

    @Override
    public List<Payment> findExpiredPayments() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(EXPIRATION_MINUTES);
        return jpaRepository.findExpiredPayments(expirationTime);
    }

    // Métodos adicionais úteis

    public List<Payment> findExpiredPixPayments() {
        LocalDateTime expirationTime = LocalDateTime.now().minusMinutes(EXPIRATION_MINUTES);
        return jpaRepository.findExpiredPixPayments(expirationTime);
    }

    public boolean existsByExternalPaymentId(String externalPaymentId) {
        if (externalPaymentId == null || externalPaymentId.trim().isEmpty()) {
            return false;
        }
        return jpaRepository.existsByExternalPaymentId(externalPaymentId.trim());
    }

    public boolean existsByOrderId(Long orderId) {
        if (orderId == null) {
            return false;
        }
        return jpaRepository.existsByOrderId(orderId);
    }

    public Long countByStatus(PaymentStatus status) {
        if (status == null) {
            return 0L;
        }
        return jpaRepository.countByStatus(status);
    }

    // NOVO: Buscar pagamentos de um usuário específico
    public List<Payment> findByCustomer(User customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer não pode ser nulo");
        }
        return jpaRepository.findByCustomerOrderByCreatedAtDesc(customer);
    }

    // ATUALIZADO: Método inteligente para buscar por email
    public List<Payment> findByCustomerEmail(String customerEmail) {
        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Email do cliente não pode estar vazio");
        }

        String email = customerEmail.trim().toLowerCase();

        // Buscar em usuários registrados
        List<Payment> registeredPayments = jpaRepository.findByRegisteredCustomerEmail(email);

        // Buscar em pedidos de convidados (duas variações possíveis do padrão)
        String emailPattern1 = "(" + email + ")";  // padrão: nome (email@example.com)
        String emailPattern2 = email;              // padrão: email@example.com direto
        List<Payment> guestPayments = jpaRepository.findByGuestCustomerEmail(emailPattern1, emailPattern2);

        // Combinar listas e remover duplicatas
        List<Payment> allPayments = new ArrayList<>(registeredPayments);
        for (Payment guestPayment : guestPayments) {
            if (!allPayments.contains(guestPayment)) {
                allPayments.add(guestPayment);
            }
        }

        // Ordenar por data mais recente
        allPayments.sort((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()));

        return allPayments;
    }

    // NOVO: Buscar pagamentos por período
    public List<Payment> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Datas de início e fim são obrigatórias");
        }
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Data de início não pode ser posterior à data de fim");
        }
        return jpaRepository.findByDateRange(startDate, endDate);
    }
}