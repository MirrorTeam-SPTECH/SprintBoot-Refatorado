package com.exemple.apipagamento.portalchurras.application.services;

import com.exemple.apipagamento.portalchurras.domain.entities.*;
import com.exemple.apipagamento.portalchurras.domain.ports.*;
import com.exemple.apipagamento.portalchurras.domain.usecases.OrderUseCases;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OrderService implements OrderUseCases {

    private final OrderRepository orderRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository; // ADICIONADO

    public OrderService(OrderRepository orderRepository,
                        MenuItemRepository menuItemRepository,
                        UserRepository userRepository) { // ADICIONADO
        this.orderRepository = orderRepository;
        this.menuItemRepository = menuItemRepository;
        this.userRepository = userRepository;
    }

    // ALTERADO: Método para criar pedido com usuário registrado
    public Order createOrderForUser(Long customerId, String notes) {
        if (customerId == null) {
            throw new IllegalArgumentException("ID do cliente não pode ser nulo");
        }

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + customerId));

        if (!customer.getActive()) {
            throw new IllegalArgumentException("Cliente está inativo: " + customer.getEmail());
        }

        Order order = new Order(customer, BigDecimal.ZERO, notes);
        return orderRepository.save(order);
    }

    // ALTERADO: Método original atualizado para pedidos de convidados
    @Override
    public Order createOrder(String customerName, String customerEmail, String customerPhone,
                             BigDecimal total, String notes) {

        if (customerName == null || customerName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do cliente é obrigatório");
        }

        // Verificar se já existe um usuário com este email
        User existingUser = null;
        if (customerEmail != null && !customerEmail.trim().isEmpty()) {
            existingUser = userRepository.findByEmail(customerEmail.trim()).orElse(null);
        }

        if (existingUser != null) {
            // Se o usuário existe, criar pedido usando o usuário
            Order order = new Order(existingUser, total != null ? total : BigDecimal.ZERO, notes);
            return orderRepository.save(order);
        } else {
            // Criar pedido como convidado
            Order order = new Order(customerName, customerEmail, customerPhone,
                    total != null ? total : BigDecimal.ZERO, notes);
            return orderRepository.save(order);
        }
    }

    @Override
    public Order addItemToOrder(Long orderId, Long menuItemId, Integer quantity, String observations) {
        if (orderId == null) {
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        }
        if (menuItemId == null) {
            throw new IllegalArgumentException("ID do item do menu não pode ser nulo");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }

        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + orderId));

            if (!order.canBeModified()) {
                throw new IllegalStateException("Pedido não pode ser modificado no status atual: " + order.getStatus());
            }

            MenuItem menuItem = menuItemRepository.findById(menuItemId)
                    .orElseThrow(() -> new IllegalArgumentException("Item do menu não encontrado: " + menuItemId));

            if (!menuItem.getActive()) {
                throw new IllegalArgumentException("Item do menu não está ativo: " + menuItem.getName());
            }

            // Inicializar a lista de items se for null (tratamento lazy loading)
            if (order.getItems() == null) {
                order.setItems(new ArrayList<>());
            }

            order.addItem(menuItem, quantity, observations);
            return orderRepository.save(order);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao adicionar item ao pedido: " + e.getMessage(), e);
        }
    }

    @Override
    public Order removeItemFromOrder(Long orderId, Long orderItemId) {
        if (orderId == null) {
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        }
        if (orderItemId == null) {
            throw new IllegalArgumentException("ID do item não pode ser nulo");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + orderId));

        order.removeItem(orderItemId);
        return orderRepository.save(order);
    }

    @Override
    public Order updateOrderItemQuantity(Long orderId, Long orderItemId, Integer newQuantity) {
        if (orderId == null) {
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        }
        if (orderItemId == null) {
            throw new IllegalArgumentException("ID do item não pode ser nulo");
        }
        if (newQuantity == null || newQuantity <= 0) {
            throw new IllegalArgumentException("Nova quantidade deve ser maior que zero");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + orderId));

        order.updateItemQuantity(orderItemId, newQuantity);
        return orderRepository.save(order);
    }

    @Override
    public Order updateOrderStatus(Long orderId, OrderStatus newStatus) {
        if (orderId == null) {
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        }
        if (newStatus == null) {
            throw new IllegalArgumentException("Novo status não pode ser nulo");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + orderId));

        order.updateStatus(newStatus);
        return orderRepository.save(order);
    }

    @Override
    public Order updateOrderNotes(Long orderId, String notes) {
        if (orderId == null) {
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + orderId));

        order.setNotes(notes != null ? notes.trim() : null);

        return orderRepository.save(order);
    }

    @Override
    public Order cancelOrder(Long orderId, String reason) {
        if (orderId == null) {
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + orderId));

        order.cancel(reason);
        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findOrdersByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findOrdersByCustomerEmail(String customerEmail) {
        if (customerEmail == null || customerEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Email do cliente não pode estar vazio");
        }

        // Buscar por usuários registrados
        Optional<User> user = userRepository.findByEmail(customerEmail.trim());
        if (user.isPresent()) {
            return orderRepository.findByCustomer(user.get());
        } else {
            // Para pedidos de convidados, usar o método original
            return orderRepository.findByCustomerEmail(customerEmail.trim());
        }
    }

    // NOVO: Método para buscar pedidos de um usuário específico
    @Transactional(readOnly = true)
    public List<Order> findOrdersByCustomer(Long customerId) {
        if (customerId == null) {
            throw new IllegalArgumentException("ID do cliente não pode ser nulo");
        }

        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente não encontrado: " + customerId));

        return orderRepository.findByCustomer(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findOrdersByDateRange(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Datas de início e fim são obrigatórias");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Data de início não pode ser posterior à data de fim");
        }
        return orderRepository.findByCreatedAtBetween(start, end);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findActiveOrders() {
        List<OrderStatus> activeStatuses = Arrays.asList(
                OrderStatus.PENDING,
                OrderStatus.CONFIRMED,
                OrderStatus.IN_PREPARATION,
                OrderStatus.READY
        );
        return activeStatuses.stream()
                .flatMap(status -> orderRepository.findByStatus(status).stream())
                .distinct()
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calculateOrderTotal(Long orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("ID do pedido não pode ser nulo");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado: " + orderId));

        // Verificar se items não é null e tratar lazy loading
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return BigDecimal.ZERO;
        }

        return order.getItems().stream()
                .filter(item -> item != null && item.getTotalPrice() != null)
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
