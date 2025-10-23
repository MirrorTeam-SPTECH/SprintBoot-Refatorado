package com.exemple.apipagamento.portalchurras.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.Collections;

@Configuration
public class RestTemplateConfig {

    private static final Logger logger = LoggerFactory.getLogger(RestTemplateConfig.class);

    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        
        // Timeouts configurados
        factory.setConnectTimeout((int) Duration.ofSeconds(10).toMillis());
        factory.setReadTimeout((int) Duration.ofSeconds(30).toMillis());
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        // Adicionar interceptor para logging
        restTemplate.setInterceptors(Collections.singletonList(loggingInterceptor()));
        
        logger.info("RestTemplate configurado com timeouts: connect=10s, read=30s");
        
        return restTemplate;
    }
    
    private ClientHttpRequestInterceptor loggingInterceptor() {
        return (request, body, execution) -> {
            logger.debug("HTTP Request: {} {}", request.getMethod(), request.getURI());
            return execution.execute(request, body);
        };
    }
}