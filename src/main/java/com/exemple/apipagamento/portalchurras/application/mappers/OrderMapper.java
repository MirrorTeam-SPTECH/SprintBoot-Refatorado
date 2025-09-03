package com.exemple.apipagamento.portalchurras.application.mappers;

import com.exemple.apipagamento.portalchurras.application.dtos.OrderDTO;
import com.exemple.apipagamento.portalchurras.application.dtos.OrderItemDTO;
import com.exemple.apipagamento.portalchurras.application.dtos.PaymentDTO;
import com.exemple.apipagamento.portalchurras.domain.entities.Order;
import com.exemple.apipagamento.portalchurras.domain.entities.OrderItem;
import com.exemple.apipagamento.portalchurras.domain.entities.Payment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderDTO toDTO(Order order) {
        if (order == null) {
            return null;
        }

        try {
            OrderDTO dto = new OrderDTO();
            dto.setId(order.getId());
            dto.setCustomerName(order.getCustomerName());
            dto.setCustomerEmail(order.getCustomerEmail());
            dto.setCustomerPhone(order.getCustomerPhone());
            dto.setTotal(order.getTotal());
            dto.setStatus(order.getStatus());
            dto.setNotes(order.getNotes());
            dto.setCreatedAt(order.getCreatedAt());
            dto.setUpdatedAt(order.getUpdatedAt());

            // Mapear itens com filtragem de nulos
            if (order.getItems() != null) {
                List<OrderItemDTO> itemDTOs = order.getItems().stream()
                        .map(this::toOrderItemDTO)
                        .filter(item -> item != null)
                        .collect(Collectors.toList());
                dto.setItems(itemDTOs);
            }

            // Mapear pagamento
            if (order.getPayment() != null) {
                dto.setPayment(toPaymentDTO(order.getPayment()));
            }

            return dto;
        } catch (Exception e) {
            throw new RuntimeException("Erro ao mapear Order para OrderDTO: " + e.getMessage(), e);
        }
    }

    public OrderItemDTO toOrderItemDTO(OrderItem orderItem) {
        if (orderItem == null) {
            return null;
        }

        try {
            OrderItemDTO dto = new OrderItemDTO();
            dto.setId(orderItem.getId());
            
            // Verificação null para MenuItem
            if (orderItem.getMenuItem() != null) {
                dto.setMenuItemId(orderItem.getMenuItem().getId());
                dto.setMenuItemName(orderItem.getMenuItem().getName());
            } else {
                // Log de warning se MenuItem for null (situação não esperada)
                System.err.println("Warning: OrderItem " + orderItem.getId() + " tem MenuItem null");
            }
            
            dto.setQuantity(orderItem.getQuantity() != null ? orderItem.getQuantity() : 0);
            dto.setUnitPrice(orderItem.getUnitPrice());
            dto.setTotalPrice(orderItem.getTotalPrice());
            dto.setObservations(orderItem.getObservations());

            return dto;
        } catch (Exception e) {
            System.err.println("Erro ao mapear OrderItem para OrderItemDTO: " + e.getMessage());
            return null; // Retorna null para ser filtrado no stream
        }
    }

    public PaymentDTO toPaymentDTO(Payment payment) {
        if (payment == null) {
            return null;
        }

        PaymentDTO dto = new PaymentDTO();
        dto.setId(payment.getId());
        dto.setMethod(payment.getMethod());
        dto.setStatus(payment.getStatus());
        dto.setAmount(payment.getAmount());
        dto.setExternalPaymentId(payment.getExternalPaymentId());
        dto.setQrCode(payment.getQrCode());
        dto.setQrCodeBase64(payment.getQrCodeBase64());
        dto.setTicketUrl(payment.getTicketUrl());
        dto.setFailureReason(payment.getFailureReason());
        dto.setCreatedAt(payment.getCreatedAt());
        dto.setPaidAt(payment.getPaidAt());

        return dto;
    }

    public List<OrderDTO> toDTOList(List<Order> orders) {
        if (orders == null) {
            return null;
        }

        try {
            return orders.stream()
                    .map(this::toDTO)
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao mapear lista de Orders para OrderDTOs: " + e.getMessage(), e);
        }
    }
}
