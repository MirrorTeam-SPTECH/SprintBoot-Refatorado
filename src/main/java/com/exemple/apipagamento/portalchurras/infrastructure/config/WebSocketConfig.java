package com.exemple.apipagamento.portalchurras.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuração de WebSocket para comunicação em tempo real.
 * Utilizado para notificações de status de pedidos e atualizações de menu.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${websocket.allowed.origins:http://localhost:3000,http://localhost:4200}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Configurar broker de mensagens simples em memória
        // /topic - para broadcast (mensagens para múltiplos usuários)
        // /queue - para mensagens ponto-a-ponto
        config.enableSimpleBroker("/topic", "/queue");
        
        // Prefixo para mensagens enviadas do cliente para o servidor
        config.setApplicationDestinationPrefixes("/app");
        
        // Prefixo para mensagens direcionadas a usuários específicos
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Endpoint STOMP com fallback SockJS para clientes web
        // Permite conexão de navegadores antigos sem suporte nativo a WebSocket
        registry.addEndpoint("/ws")
            .setAllowedOrigins(allowedOrigins.split(","))
            .withSockJS();
            
        // Endpoint WebSocket nativo para clientes com suporte completo
        registry.addEndpoint("/ws-native")
            .setAllowedOrigins(allowedOrigins.split(","));
    }
}