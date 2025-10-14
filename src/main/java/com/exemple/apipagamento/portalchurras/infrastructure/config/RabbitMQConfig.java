package com.exemple.apipagamento.portalchurras.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.orders}")
    private String ordersExchange;

    @Value("${rabbitmq.queue.new-orders}")
    private String newOrdersQueue;

    @Value("${rabbitmq.queue.order-status}")
    private String orderStatusQueue;

    @Value("${rabbitmq.queue.notifications}")
    private String notificationsQueue;

    @Value("${rabbitmq.queue.loyalty-points}")
    private String loyaltyPointsQueue;

    @Value("${rabbitmq.routing-key.new-order}")
    private String newOrderRoutingKey;

    @Value("${rabbitmq.routing-key.order-status}")
    private String orderStatusRoutingKey;

    @Value("${rabbitmq.routing-key.notification}")
    private String notificationRoutingKey;

    @Value("${rabbitmq.routing-key.loyalty}")
    private String loyaltyRoutingKey;

    // Exchange
    @Bean
    public TopicExchange ordersExchange() {
        return new TopicExchange(ordersExchange);
    }

    // Queues
    @Bean
    public Queue newOrdersQueue() {
        return QueueBuilder.durable(newOrdersQueue)
                .withArgument("x-message-ttl", 3600000) // 1 hour TTL
                .build();
    }

    @Bean
    public Queue orderStatusQueue() {
        return QueueBuilder.durable(orderStatusQueue).build();
    }

    @Bean
    public Queue notificationsQueue() {
        return QueueBuilder.durable(notificationsQueue).build();
    }

    @Bean
    public Queue loyaltyPointsQueue() {
        return QueueBuilder.durable(loyaltyPointsQueue).build();
    }

    // Bindings
    @Bean
    public Binding newOrderBinding() {
        return BindingBuilder.bind(newOrdersQueue())
                .to(ordersExchange())
                .with(newOrderRoutingKey);
    }

    @Bean
    public Binding orderStatusBinding() {
        return BindingBuilder.bind(orderStatusQueue())
                .to(ordersExchange())
                .with(orderStatusRoutingKey);
    }

    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationsQueue())
                .to(ordersExchange())
                .with(notificationRoutingKey);
    }

    @Bean
    public Binding loyaltyBinding() {
        return BindingBuilder.bind(loyaltyPointsQueue())
                .to(ordersExchange())
                .with(loyaltyRoutingKey);
    }

    // Message Converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // RabbitTemplate
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
