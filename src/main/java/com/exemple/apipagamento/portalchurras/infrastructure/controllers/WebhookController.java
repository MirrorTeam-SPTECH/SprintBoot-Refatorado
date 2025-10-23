package com.exemple.apipagamento.portalchurras.infrastructure.controllers;

import com.exemple.apipagamento.portalchurras.domain.usecases.PaymentUseCases;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/webhooks")
@Tag(name = "Webhooks", description = "API para recebimento de webhooks")
public class WebhookController {

    private static final Logger logger = LoggerFactory.getLogger(WebhookController.class);
    
    private final PaymentUseCases paymentUseCases;

    public WebhookController(PaymentUseCases paymentUseCases) {
        this.paymentUseCases = paymentUseCases;
    }

    @PostMapping("/mercadopago")
    @Operation(summary = "Receber webhook do Mercado Pago")
    public ResponseEntity<?> receiveMercadoPagoWebhook(@RequestBody Map<String, Object> payload) {
        try {
            logger.info("Webhook Mercado Pago recebido: {}", payload);

            // Processar webhook
            paymentUseCases.processWebhook(payload);

            return ResponseEntity.ok().build();

        } catch (IllegalArgumentException e) {
            logger.warn("Webhook inválido: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            logger.error("Erro ao processar webhook", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/mercadopago/test")
    @Operation(summary = "Endpoint de teste para validar webhook")
    public ResponseEntity<Map<String, String>> testWebhook() {
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Webhook endpoint está funcionando",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}
