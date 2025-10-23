package com.exemple.apipagamento.portalchurras.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuração da documentação OpenAPI/Swagger.
 * Acesse em: /swagger-ui.html
 */
@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Portal Churras API")
                        .description("""
                                API RESTful completa para gerenciamento de food truck.
                                
                                ## Funcionalidades:
                                - 🔐 **Autenticação JWT** - Login seguro com tokens Bearer
                                - 📋 **Cardápio** - CRUD completo de itens do menu
                                - 🛒 **Pedidos** - Gestão de pedidos com itens
                                - 💳 **Pagamentos** - Integração com Mercado Pago (PIX e preferências)
                                - 👥 **Usuários** - Gestão de clientes e funcionários
                                - 🔔 **Webhooks** - Notificações de pagamento
                                
                                ## Como usar:
                                1. Faça login em `/api/auth/login` para obter o token
                                2. Clique em **Authorize** no topo da página
                                3. Cole o token (sem 'Bearer ') e clique em **Authorize**
                                4. Agora você pode testar os endpoints protegidos
                                
                                ## Perfis de usuário:
                                - **CUSTOMER** - Cliente (pode fazer pedidos)
                                - **EMPLOYEE** - Funcionário (gerencia pedidos e pagamentos)
                                - **ADMIN** - Administrador (acesso total)
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("MirrorTeam - SPTECH")
                                .email("suporte@portalchurras.com")
                                .url("https://github.com/MirrorTeam-SPTECH"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("Servidor de Desenvolvimento"),
                        new Server()
                                .url("https://api.portalchurras.com")
                                .description("Servidor de Produção")
                ))
                .components(new Components()
                        .addSecuritySchemes("Bearer", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Token JWT obtido através do endpoint /api/auth/login")));
    }
}
