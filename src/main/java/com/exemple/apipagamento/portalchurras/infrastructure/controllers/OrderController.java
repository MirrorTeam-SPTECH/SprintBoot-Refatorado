package com.exemple.apipagamento.portalchurras.infrastructure.controllers;

import com.exemple.apipagamento.portalchurras.application.dtos.*;
import com.exemple.apipagamento.portalchurras.application.mappers.OrderMapper;
import com.exemple.apipagamento.portalchurras.domain.entities.Order;
import com.exemple.apipagamento.portalchurras.domain.entities.OrderStatus;
import com.exemple.apipagamento.portalchurras.domain.usecases.OrderUseCases;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "API para gerenciamento de pedidos")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class OrderController {

    private final OrderUseCases orderUseCases;
    private final OrderMapper orderMapper;

    public OrderController(OrderUseCases orderUseCases, OrderMapper orderMapper) {
        this.orderUseCases = orderUseCases;
        this.orderMapper = orderMapper;
    }

    @PostMapping
    @Operation(summary = "Criar novo pedido")
    @ApiResponse(responseCode = "201", description = "Pedido criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        try {
            Order order = orderUseCases.createOrder(
                    request.getCustomerName(),
                    request.getCustomerEmail(),
                    request.getCustomerPhone(),
                    request.getTotal(),
                    request.getNotes()
            );

            OrderDTO orderDTO = orderMapper.toDTO(order);
            return ResponseEntity.status(HttpStatus.CREATED).body(orderDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @PostMapping("/{orderId}/items")
    @Operation(summary = "Adicionar item ao pedido")
    @ApiResponse(responseCode = "200", description = "Item adicionado com sucesso")
    @ApiResponse(responseCode = "400", description = "Pedido não pode ser modificado ou dados inválidos")
    @ApiResponse(responseCode = "404", description = "Pedido ou item do cardápio não encontrado")
    public ResponseEntity<?> addItemToOrder(
            @Parameter(description = "ID do pedido") @PathVariable Long orderId,
            @Valid @RequestBody AddOrderItemRequest request) {

        try {
            Order order = orderUseCases.addItemToOrder(
                    orderId,
                    request.getMenuItemId(),
                    request.getQuantity(),
                    request.getObservations()
            );

            OrderDTO orderDTO = orderMapper.toDTO(order);
            return ResponseEntity.ok(orderDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    @Operation(summary = "Remover item do pedido")
    @ApiResponse(responseCode = "200", description = "Item removido com sucesso")
    @ApiResponse(responseCode = "400", description = "Pedido não pode ser modificado")
    @ApiResponse(responseCode = "404", description = "Pedido ou item não encontrado")
    public ResponseEntity<?> removeItemFromOrder(
            @PathVariable Long orderId,
            @PathVariable Long itemId) {

        try {
            Order order = orderUseCases.removeItemFromOrder(orderId, itemId);
            OrderDTO orderDTO = orderMapper.toDTO(order);
            return ResponseEntity.ok(orderDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @PutMapping("/{orderId}/items/{itemId}/quantity")
    @Operation(summary = "Atualizar quantidade do item no pedido")
    public ResponseEntity<?> updateItemQuantity(
            @PathVariable Long orderId,
            @PathVariable Long itemId,
            @RequestBody Map<String, Integer> quantityData) {

        try {
            Integer newQuantity = quantityData.get("quantity");
            if (newQuantity == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Quantidade é obrigatória"));
            }

            Order order = orderUseCases.updateOrderItemQuantity(orderId, itemId, newQuantity);
            OrderDTO orderDTO = orderMapper.toDTO(order);
            return ResponseEntity.ok(orderDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @PatchMapping("/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @Operation(summary = "Atualizar status do pedido",
            security = @SecurityRequirement(name = "Bearer"))
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> statusData) {

        try {
            String statusStr = statusData.get("status");
            if (statusStr == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Status é obrigatório"));
            }

            OrderStatus newStatus = OrderStatus.valueOf(statusStr.toUpperCase());
            Order order = orderUseCases.updateOrderStatus(orderId, newStatus);
            OrderDTO orderDTO = orderMapper.toDTO(order);
            return ResponseEntity.ok(orderDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @PatchMapping("/{orderId}/cancel")
    @Operation(summary = "Cancelar pedido")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long orderId,
            @RequestBody(required = false) Map<String, String> cancelData) {

        try {
            String reason = cancelData != null ? cancelData.get("reason") : null;
            Order order = orderUseCases.cancelOrder(orderId, reason);
            OrderDTO orderDTO = orderMapper.toDTO(order);
            return ResponseEntity.ok(orderDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @PatchMapping("/{orderId}/notes")
    @Operation(summary = "Atualizar observações do pedido")
    public ResponseEntity<?> updateOrderNotes(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> notesData) {

        try {
            String notes = notesData.get("notes");
            Order order = orderUseCases.updateOrderNotes(orderId, notes);
            OrderDTO orderDTO = orderMapper.toDTO(order);
            return ResponseEntity.ok(orderDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Buscar pedido por ID")
    @ApiResponse(responseCode = "200", description = "Pedido encontrado")
    @ApiResponse(responseCode = "404", description = "Pedido não encontrado")
    public ResponseEntity<?> getOrderById(@PathVariable Long orderId) {
        try {
            return orderUseCases.findOrderById(orderId)
                    .map(order -> ResponseEntity.ok(orderMapper.toDTO(order)))
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @Operation(summary = "Listar todos os pedidos",
            security = @SecurityRequirement(name = "Bearer"))
    public ResponseEntity<?> getAllOrders() {
        try {
            List<Order> orders = orderUseCases.findAllOrders();
            List<OrderDTO> orderDTOs = orderMapper.toDTOList(orders);
            return ResponseEntity.ok(orderDTOs);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @Operation(summary = "Listar pedidos ativos (em andamento)",
            security = @SecurityRequirement(name = "Bearer"))
    public ResponseEntity<?> getActiveOrders() {
        try {
            List<Order> orders = orderUseCases.findActiveOrders();
            List<OrderDTO> orderDTOs = orderMapper.toDTOList(orders);
            return ResponseEntity.ok(orderDTOs);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @Operation(summary = "Listar pedidos por status",
            security = @SecurityRequirement(name = "Bearer"))
    public ResponseEntity<?> getOrdersByStatus(@PathVariable OrderStatus status) {
        try {
            List<Order> orders = orderUseCases.findOrdersByStatus(status);
            List<OrderDTO> orderDTOs = orderMapper.toDTOList(orders);
            return ResponseEntity.ok(orderDTOs);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @GetMapping("/customer/{email}")
    @Operation(summary = "Listar pedidos de um cliente por email")
    public ResponseEntity<?> getOrdersByCustomerEmail(@PathVariable String email) {
        try {
            List<Order> orders = orderUseCases.findOrdersByCustomerEmail(email);
            List<OrderDTO> orderDTOs = orderMapper.toDTOList(orders);
            return ResponseEntity.ok(orderDTOs);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasRole('ADMIN') or hasRole('EMPLOYEE')")
    @Operation(summary = "Listar pedidos por período",
            security = @SecurityRequirement(name = "Bearer"))
    public ResponseEntity<?> getOrdersByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        try {
            List<Order> orders = orderUseCases.findOrdersByDateRange(start, end);
            List<OrderDTO> orderDTOs = orderMapper.toDTOList(orders);
            return ResponseEntity.ok(orderDTOs);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }
}