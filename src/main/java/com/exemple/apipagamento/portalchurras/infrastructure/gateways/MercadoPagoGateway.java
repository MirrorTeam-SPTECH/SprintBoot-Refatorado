package com.exemple.apipagamento.portalchurras.infrastructure.gateways;

import com.exemple.apipagamento.portalchurras.infrastructure.config.MercadoPagoProperties;
import com.exemple.apipagamento.portalchurras.domain.ports.*;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class MercadoPagoGateway implements PaymentGateway {

    private final RestTemplate restTemplate;
    private final MercadoPagoProperties properties;

    public MercadoPagoGateway(RestTemplate restTemplate, MercadoPagoProperties properties) {
        this.restTemplate = restTemplate;
        this.properties = properties;
    }

    @Override
    public PaymentGatewayResponse createPaymentPreference(PaymentGatewayRequest request) {
        if (request == null) {
            return PaymentGatewayResponse.error("Request não pode ser nulo");
        }
        if (request.getItems() == null || request.getItems().isEmpty()) {
            return PaymentGatewayResponse.error("Itens são obrigatórios");
        }
        if (properties.getAccessToken() == null || properties.getAccessToken().trim().isEmpty()) {
            return PaymentGatewayResponse.error("Access token do Mercado Pago não configurado");
        }

        try {
            String url = properties.getBaseUrl() + "/checkout/preferences";

            HttpHeaders headers = createHeaders();
            Map<String, Object> body = new HashMap<>();

            // Converter itens
            body.put("items", request.getItems().stream()
                    .map(item -> Map.of(
                            "title", item.getTitle() != null ? item.getTitle() : "Item",
                            "quantity", item.getQuantity() != null ? item.getQuantity() : 1,
                            "unit_price", item.getUnitPrice() != null ? item.getUnitPrice() : 0.0
                    ))
                    .collect(Collectors.toList()));

            // URLs de retorno
            Map<String, String> backUrls = new HashMap<>();
            backUrls.put("success", properties.getSuccessUrl());
            backUrls.put("failure", properties.getFailureUrl());
            backUrls.put("pending", properties.getPendingUrl());
            body.put("back_urls", backUrls);
            body.put("auto_return", "approved");

            // External reference para rastrear o pedido
            if (request.getOrderId() != null) {
                body.put("external_reference", request.getOrderId().toString());
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = response.getBody();
                return buildSuccessResponse(responseBody);
            } else {
                return PaymentGatewayResponse.error("Resposta inválida do Mercado Pago");
            }

        } catch (Exception e) {
            return PaymentGatewayResponse.error("Erro ao criar preferência: " + e.getMessage());
        }
    }

    @Override
    public PaymentGatewayResponse createPixPayment(PaymentGatewayRequest request) {
        if (request == null) {
            return PaymentGatewayResponse.error("Request não pode ser nulo");
        }
        if (request.getAmount() == null || request.getAmount().doubleValue() <= 0) {
            return PaymentGatewayResponse.error("Valor deve ser maior que zero");
        }
        
        // Modo demo: retornar QR Code mock quando não há token configurado
        if (properties.getAccessToken() == null || properties.getAccessToken().trim().isEmpty()) {
            return createMockPixResponse(request);
        }

        try {
            String url = properties.getBaseUrl() + "/v1/payments";

            HttpHeaders headers = createHeaders();
            headers.set("X-Idempotency-Key", UUID.randomUUID().toString());

            Map<String, Object> body = new HashMap<>();
            body.put("transaction_amount", request.getAmount());
            body.put("description", request.getDescription() != null ? request.getDescription() : "Pagamento PIX");
            body.put("payment_method_id", "pix");

            if (request.getCustomerEmail() != null && !request.getCustomerEmail().trim().isEmpty()) {
                Map<String, String> payer = new HashMap<>();
                payer.put("email", request.getCustomerEmail().trim());
                body.put("payer", payer);
            }

            // External reference para rastrear o pedido
            if (request.getOrderId() != null) {
                body.put("external_reference", request.getOrderId().toString());
            }

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                @SuppressWarnings("unchecked")
                Map<String, Object> responseBody = response.getBody();
                return buildPixResponse(responseBody);
            } else {
                return PaymentGatewayResponse.error("Resposta inválida do Mercado Pago para PIX");
            }

        } catch (Exception e) {
            return PaymentGatewayResponse.error("Erro ao criar pagamento PIX: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public PaymentGatewayResponse getPaymentStatus(String externalPaymentId) {
        if (externalPaymentId == null || externalPaymentId.trim().isEmpty()) {
            return PaymentGatewayResponse.error("ID do pagamento não pode ser nulo ou vazio");
        }
        if (properties.getAccessToken() == null || properties.getAccessToken().trim().isEmpty()) {
            return PaymentGatewayResponse.error("Access token do Mercado Pago não configurado");
        }

        try {
            String url = properties.getBaseUrl() + "/v1/payments/" + externalPaymentId.trim();

            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                Map<String, Object> body = response.getBody();
                if (body != null) {
                    PaymentGatewayResponse result = new PaymentGatewayResponse();
                    result.setSuccess(true);
                    result.setExternalPaymentId(externalPaymentId);
                    result.setStatus((String) body.get("status"));
                    result.setRawResponse(body);
                    return result;
                }
            }
            
            return PaymentGatewayResponse.error("Resposta inválida do Mercado Pago ao consultar status");

        } catch (Exception e) {
            return PaymentGatewayResponse.error("Erro ao consultar status: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public PaymentGatewayResponse cancelPayment(String externalPaymentId) {
        try {
            String url = "https://api.mercadopago.com/v1/payments/" + externalPaymentId;

            HttpHeaders headers = createHeaders();
            Map<String, Object> body = Map.of("status", "cancelled");

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.PUT, entity, Map.class);

            PaymentGatewayResponse result = new PaymentGatewayResponse();
            result.setSuccess(true);
            result.setExternalPaymentId(externalPaymentId);
            result.setStatus("cancelled");
            result.setRawResponse(response.getBody());

            return result;

        } catch (Exception e) {
            return PaymentGatewayResponse.error("Erro ao cancelar pagamento: " + e.getMessage());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public PaymentWebhookResponse processWebhook(Map<String, Object> webhookData) {
        try {
            String action = (String) webhookData.get("action");
            String type = (String) webhookData.get("type");

            if (!"payment".equals(type)) {
                return new PaymentWebhookResponse(false, null, null);
            }

            Map<String, Object> data = (Map<String, Object>) webhookData.get("data");
            String paymentId = (String) data.get("id");

            // Consultar o status atual do pagamento
            PaymentGatewayResponse statusResponse = getPaymentStatus(paymentId);

            if (!statusResponse.isSuccess()) {
                return new PaymentWebhookResponse(false, paymentId, null);
            }

            PaymentWebhookResponse result = new PaymentWebhookResponse();
            result.setValid(true);
            result.setExternalPaymentId(paymentId);
            result.setStatus(statusResponse.getStatus());
            result.setAction(action);
            result.setType(type);

            return result;

        } catch (Exception e) {
            return new PaymentWebhookResponse(false, null, "Erro ao processar webhook: " + e.getMessage());
        }
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String accessToken = properties.getAccessToken();
        if (accessToken != null && !accessToken.trim().isEmpty()) {
            headers.setBearerAuth(accessToken.trim());
        }
        
        return headers;
    }

    private PaymentGatewayResponse buildSuccessResponse(Map<String, Object> responseBody) {
        PaymentGatewayResponse result = new PaymentGatewayResponse();
        result.setSuccess(true);
        result.setExternalPreferenceId((String) responseBody.get("id"));
        result.setInitPoint((String) responseBody.get("init_point"));
        result.setRawResponse(responseBody);
        return result;
    }

    @SuppressWarnings("unchecked")
    private PaymentGatewayResponse buildPixResponse(Map<String, Object> responseBody) {
        PaymentGatewayResponse result = new PaymentGatewayResponse();
        
        if (responseBody == null) {
            result.setSuccess(false);
            result.setErrorMessage("Resposta vazia do Mercado Pago");
            return result;
        }
        
        result.setSuccess(true);
        
        // ID do pagamento
        Object idObj = responseBody.get("id");
        if (idObj != null) {
            result.setExternalPaymentId(idObj.toString());
        }
        
        // Status do pagamento
        result.setStatus((String) responseBody.get("status"));

        // Extrair dados do PIX com verificações de null
        Map<String, Object> pointOfInteraction = (Map<String, Object>) responseBody.get("point_of_interaction");
        if (pointOfInteraction != null) {
            Map<String, Object> transactionData = (Map<String, Object>) pointOfInteraction.get("transaction_data");
            if (transactionData != null) {
                result.setQrCode((String) transactionData.get("qr_code"));
                result.setQrCodeBase64((String) transactionData.get("qr_code_base64"));
            }
        }

        Map<String, Object> transactionDetails = (Map<String, Object>) responseBody.get("transaction_details");
        if (transactionDetails != null) {
            result.setTicketUrl((String) transactionDetails.get("external_resource_url"));
        }

        result.setRawResponse(responseBody);
        return result;
    }
    
    /**
     * Cria uma resposta mock de PIX para ambiente de demonstração
     */
    private PaymentGatewayResponse createMockPixResponse(PaymentGatewayRequest request) {
        PaymentGatewayResponse response = new PaymentGatewayResponse();
        response.setSuccess(true);
        response.setExternalPaymentId("DEMO-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        response.setStatus("pending");
        
        // QR Code PIX simulado (formato EMV)
        String pixCode = String.format(
            "00020126580014br.gov.bcb.pix0136%s5204000053039865406%.2f5802BR5925PORTAL DO CHURRAS DEMO6009SAO PAULO62070503***6304",
            UUID.randomUUID().toString(),
            request.getAmount().doubleValue()
        ).replace(",", ".");
        
        response.setQrCode(pixCode);
        
        // QR Code Base64 - imagem PNG simples de demonstração
        // Esta é uma imagem placeholder pequena
        response.setQrCodeBase64("iVBORw0KGgoAAAANSUhEUgAAAMgAAADICAYAAACtWK6eAAABhGlDQ1BJQ0MgcHJvZmlsZQAAKJF9kT1Iw0AcxV9TpSIVBzuIOGSoThZERRy1CkWoEGqFVh1MLv2CJg1Jiouj4Fpw8GOx6uDirKuDqyAIfoC4uTkpukiJ/0sKLWI8OO7Hu3uPu3eA0Kgy1eyZAFTNMlLxmJjNrYqBV/gxjAGEEZWYqSfTixl4jq97+Ph6F+FZ3uf+HP1K3mSATySeY7phEW8Qz2xaOud94jArSQrxOfGEQRckfuS67PIb56LDAs8MG5n0PHGYWCx2sNzBrGSoxFPEUUXVKF/Iuqxw3uKsVmqsdU/+wmBeW1nmOs0RxLGEBJIQIaOGMiqwEKVVI8VEms5jHv6Rxp8kLplcZTByLKAKFZLjB/+D392ahckJNymcAHpfbPsjCgR2gVbDtr+Pbbt1AvifgSut4681gdlP0hsdLXoEDGwDF9cdTd4DLneA4SddMiRH8tMUikXg/Yy+KQcM3gK9a25vrX2cPgBp6ip5AxwcAmMlyl73eHdfd2//nmn39wO2VXLP");
        
        response.setTicketUrl("https://portalchurras.demo/pix/" + response.getExternalPaymentId());
        
        Map<String, Object> rawResponse = new HashMap<>();
        rawResponse.put("id", response.getExternalPaymentId());
        rawResponse.put("status", "pending");
        rawResponse.put("demo_mode", true);
        rawResponse.put("message", "Modo demonstração - PIX simulado");
        response.setRawResponse(rawResponse);
        
        return response;
    }
}
