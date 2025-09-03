package com.exemple.apipagamento.portalchurras.domain.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ALTERAÇÃO: Substituir campos individuais por referência ao User
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private User customer;

    @NotNull(message = "Total é obrigatório")
    @DecimalMin(value = "0.01", message = "Total deve ser maior que zero")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Payment payment;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    protected Order() {}

    // ALTERAÇÃO: Novo construtor usando User
    public Order(User customer, BigDecimal total, String notes) {
        this.customer = customer; // Pode ser null para pedidos sem cadastro
        this.total = total != null ? total : BigDecimal.ZERO;
        this.notes = notes;
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Construtor alternativo para pedidos sem usuário cadastrado
    public Order(String guestName, String guestEmail, String guestPhone, BigDecimal total, String notes) {
        // Para pedidos de convidados, criar um objeto User temporário
        // Ou manter campos separados para guests
        this.total = total != null ? total : BigDecimal.ZERO;
        this.notes = notes;
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();

        // Armazenar dados do guest nas observações se necessário
        if (guestName != null) {
            String guestInfo = "Guest: " + guestName;
            if (guestEmail != null) guestInfo += " (" + guestEmail + ")";
            if (guestPhone != null) guestInfo += " - " + guestPhone;

            this.notes = notes != null ? notes + " | " + guestInfo : guestInfo;
        }
    }

    // Métodos de negócio para gerenciar itens
    public void addItem(MenuItem menuItem, Integer quantity, String observations) {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Não é possível adicionar itens a um pedido que não está pendente");
        }

        // Verificar se o item já existe
        OrderItem existingItem = this.items.stream()
                .filter(item -> item.getMenuItem().getId().equals(menuItem.getId()))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            if (observations != null) {
                String currentObs = existingItem.getObservations();
                existingItem.setObservations(currentObs != null ? currentObs + " | " + observations : observations);
            }
        } else {
            OrderItem newItem = new OrderItem(this, menuItem, quantity, menuItem.getPrice());
            newItem.setObservations(observations);
            this.items.add(newItem);
        }

        recalculateTotal();
    }

    public void removeItem(Long orderItemId) {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Não é possível remover itens de um pedido que não está pendente");
        }

        this.items.removeIf(item -> item.getId().equals(orderItemId));
        recalculateTotal();
    }

    public void updateItemQuantity(Long orderItemId, Integer newQuantity) {
        if (this.status != OrderStatus.PENDING) {
            throw new IllegalStateException("Não é possível alterar quantidades de um pedido que não está pendente");
        }

        OrderItem item = this.items.stream()
                .filter(i -> i.getId().equals(orderItemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Item não encontrado no pedido"));

        item.setQuantity(newQuantity);
        recalculateTotal();
    }

    public void updateStatus(OrderStatus newStatus) {
        if (!isValidStatusTransition(this.status, newStatus)) {
            throw new IllegalStateException("Transição de status inválida: " + this.status + " -> " + newStatus);
        }

        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel(String reason) {
        if (!this.status.canBeCancelled()) {
            throw new IllegalStateException("Pedido não pode ser cancelado no status atual: " + this.status);
        }

        this.status = OrderStatus.CANCELLED;

        String cancellationNote = "CANCELADO: " + (reason != null ? reason : "Sem motivo informado");
        this.notes = this.notes != null ? this.notes + " | " + cancellationNote : cancellationNote;
        this.updatedAt = LocalDateTime.now();
    }

    private void recalculateTotal() {
        BigDecimal newTotal = this.items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.total = newTotal;
        this.updatedAt = LocalDateTime.now();
    }

    private boolean isValidStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        return switch (currentStatus) {
            case PENDING -> newStatus == OrderStatus.CONFIRMED || newStatus == OrderStatus.CANCELLED;
            case CONFIRMED -> newStatus == OrderStatus.IN_PREPARATION || newStatus == OrderStatus.CANCELLED;
            case IN_PREPARATION -> newStatus == OrderStatus.READY;
            case READY -> newStatus == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false; // Estados finais
        };
    }

    // Métodos de conveniência para acessar dados do cliente
    public String getCustomerName() {
        return customer != null ? customer.getName() : extractGuestName();
    }

    public String getCustomerEmail() {
        return customer != null ? customer.getEmail() : extractGuestEmail();
    }

    public String getCustomerPhone() {
        return customer != null ? customer.getPhone() : extractGuestPhone();
    }

    private String extractGuestName() {
        if (notes != null && notes.contains("Guest: ")) {
            String guestPart = notes.substring(notes.indexOf("Guest: ") + 7);
            int endIndex = guestPart.indexOf(" (");
            return endIndex > 0 ? guestPart.substring(0, endIndex) : guestPart.split(" - ")[0];
        }
        return null;
    }

    private String extractGuestEmail() {
        if (notes != null && notes.contains(" (") && notes.contains(")")) {
            int startIndex = notes.indexOf(" (") + 2;
            int endIndex = notes.indexOf(")", startIndex);
            return endIndex > startIndex ? notes.substring(startIndex, endIndex) : null;
        }
        return null;
    }

    private String extractGuestPhone() {
        if (notes != null && notes.contains(" - ")) {
            String[] parts = notes.split(" - ");
            if (parts.length > 1) {
                return parts[1].split(" \\|")[0].trim();
            }
        }
        return null;
    }

    public boolean hasRegisteredCustomer() {
        return customer != null;
    }

    public boolean canBeModified() {
        return status == OrderStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = OrderStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Getters e Setters
    public Long getId() { return id; }
    public User getCustomer() { return customer; }
    public void setCustomer(User customer) { this.customer = customer; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
    public Payment getPayment() { return payment; }
    public void setPayment(Payment payment) { this.payment = payment; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}