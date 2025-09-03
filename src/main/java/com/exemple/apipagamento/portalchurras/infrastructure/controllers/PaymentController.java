package com.exemple.apipagamento.portalchurras.infrastructure.controllers;

import com.exemple.apipagamento.portalchurras.application.dtos.PaymentDTO;
import com.exemple.apipagamento.portalchurras.application.mappers.OrderMapper;
import com.exemple.apipagamento.portalchurras.domain.entities.Payment;
import com.exemple.apipagamento.portalchurras.domain.entities.PaymentMethod;
import com.exemple.apipagamento.portalchurras.domain.usecases.PaymentUseCases;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payments", description = "API para processamento de pagamentos")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174"})
public class PaymentController {

    private final PaymentUseCases paymentUseCases;
    private final OrderMapper orderMapper;

    public PaymentController(PaymentUseCases paymentUseCases, OrderMapper orderMapper) {
        this.paymentUseCases = paymentUseCases;
        this.orderMapper = orderMapper;
    }

    @PostMapping
    @Operation(summary = "Criar pagamento para um pedido")
    @ApiResponse(responseCode = "201", description = "Pagamento criado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados inválidos")
    @ApiResponse(responseCode = "409", description = "Já existe um pagamento ativo para este pedido")
    public ResponseEntity<?> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        try {
            Payment payment = paymentUseCases.createPayment(
                    request.getOrderId(),
                    request.getMethod(),
                    request.getAmount()
            );

            PaymentDTO paymentDTO = orderMapper.toPaymentDTO(payment);
            return ResponseEntity.status(HttpStatus.CREATED).body(paymentDTO);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @PostMapping("/{paymentId}/process")
    @Operation(summary = "Processar pagamento (gerar preferência do Mercado Pago)")
    @ApiResponse(responseCode = "200", description = "Pagamento processado com sucesso")
    @ApiResponse(responseCode = "404", description = "Pagamento não encontrado")
    public ResponseEntity<?> processPayment(
            @Parameter(description = "ID do pagamento") @PathVariable Long paymentId) {

        try {
            Payment payment = paymentUseCases.processPayment(paymentId);
            PaymentDTO paymentDTO = orderMapper.toPaymentDTO(payment);

            return ResponseEntity.ok(paymentDTO);

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

    @PostMapping("/{paymentId}/pix")
    @Operation(summary = "Processar pagamento PIX")
    @ApiResponse(responseCode = "200", description = "QR Code PIX gerado com sucesso")
    @ApiResponse(responseCode = "400", description = "Pagamento não é do tipo PIX ou dados inválidos")
    public ResponseEntity<?> processPixPayment(
            @PathVariable Long paymentId,
            @RequestBody PixPaymentRequest request) {

        try {
            Payment payment = paymentUseCases.createPixPayment(paymentId, request.getCustomerEmail());
            PaymentDTO paymentDTO = orderMapper.toPaymentDTO(payment);

            // Resposta específica para PIX
            Map<String, Object> response = Map.of(
                    "payment", paymentDTO,
                    "qr_code", payment.getQrCode() != null ? payment.getQrCode() : "",
                    "qr_code_base64", payment.getQrCodeBase64() != null ? payment.getQrCodeBase64() : "",
                    "ticket_url", payment.getTicketUrl() != null ? payment.getTicketUrl() : ""
            );

            return ResponseEntity.ok(response);

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

    @GetMapping("/{paymentId}")
    @Operation(summary = "Buscar pagamento por ID")
    @ApiResponse(responseCode = "200", description = "Pagamento encontrado")
    @ApiResponse(responseCode = "404", description = "Pagamento não encontrado")
    public ResponseEntity<?> getPaymentById(@PathVariable Long paymentId) {
        try {
            return paymentUseCases.findPaymentById(paymentId)
                    .map(payment -> ResponseEntity.ok(orderMapper.toPaymentDTO(payment)))
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "Buscar pagamento por ID do pedido")
    @ApiResponse(responseCode = "200", description = "Pagamento encontrado")
    @ApiResponse(responseCode = "404", description = "Pagamento não encontrado")
    public ResponseEntity<?> getPaymentByOrderId(@PathVariable Long orderId) {
        try {
            return paymentUseCases.findPaymentByOrderId(orderId)
                    .map(payment -> ResponseEntity.ok(orderMapper.toPaymentDTO(payment)))
                    .orElse(ResponseEntity.notFound().build());

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erro interno do servidor"));
        }
    }

    // DTOs internos para este controller
    public static class CreatePaymentRequest {
        @NotNull(message = "ID do pedido é obrigatório")
        private Long orderId;

        @NotNull(message = "Método de pagamento é obrigatório")
        private PaymentMethod method;

        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.01", message = "Valor deve ser maior que zero")
        private BigDecimal amount;

        // Getters e Setters
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }

        public PaymentMethod getMethod() { return method; }
        public void setMethod(PaymentMethod method) { this.method = method; }

        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }

    public static class PixPaymentRequest {
        @NotNull(message = "Email do cliente é obrigatório")
        @Email(message = "Email deve ter formato válido")
        private String customerEmail;

        // Getter e Setter
        public String getCustomerEmail() { return customerEmail; }
        public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    }
}
