package com.exemple.apipagamento.portalchurras.infrastructure.messaging;

import com.exemple.apipagamento.portalchurras.infrastructure.messaging.consumers.OrderStatusChangeEvent;
import com.exemple.apipagamento.portalchurras.infrastructure.messaging.events.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.orders}")
    private String ordersExchange;

    @Value("${rabbitmq.routing-key.new-order}")
    private String newOrderRoutingKey;

    @Value("${rabbitmq.routing-key.order-status}")
    private String orderStatusRoutingKey;

    @Value("${rabbitmq.routing-key.notification}")
    private String notificationRoutingKey;

    @Value("${rabbitmq.routing-key.loyalty}")
    private String loyaltyRoutingKey;

    public OrderEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publishNewOrder(NewOrderEvent event) {
        rabbitTemplate.convertAndSend(ordersExchange, newOrderRoutingKey, event);
    }

    public void publishOrderStatusChange(OrderStatusChangeEvent event) {
        rabbitTemplate.convertAndSend(ordersExchange, orderStatusRoutingKey, event);
    }

    public void publishNotification(NotificationEvent event) {
        rabbitTemplate.convertAndSend(ordersExchange, notificationRoutingKey, event);
    }

    public void publishLoyaltyPoints(LoyaltyPointsEvent event) {
        rabbitTemplate.convertAndSend(ordersExchange, loyaltyRoutingKey, event);
    }
}