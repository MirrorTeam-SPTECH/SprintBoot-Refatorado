package com.exemple.apipagamento.portalchurras.infrastructure.messaging.consumers;

import com.exemple.apipagamento.portalchurras.infrastructure.messaging.OrderStatusChangeEvent;
import com.exemple.apipagamento.portalchurras.infrastructure.messaging.events.NewOrderEvent;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class OrderConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderConsumer.class);

    @RabbitListener(queues = "${rabbitmq.queue.new-orders}")
    public void handleNewOrder(NewOrderEvent event) {
        logger.info("Novo pedido recebido: {}", event.getOrderId());
        
        // Processar novo pedido
        // - Enviar notificação para cozinha
        // - Atualizar dashboard
        // - Registrar métricas
    }

    @RabbitListener(queues = "${rabbitmq.queue.order-status}")
    public void handleOrderStatusChange(OrderStatusChangeEvent event) {
        logger.info("Status do pedido {} alterado para: {}", 
                   event.getOrderId(), event.getNewStatus());
        
        // Processar mudança de status
        // - Notificar cliente
        // - Atualizar dashboard
    }
}