# 🍔 Portal Churras - Backend

![Java](https://img.shields.io/badge/Java-21-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.5-brightgreen.svg)
![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-brightgreen.svg)
![Redis](https://img.shields.io/badge/Redis-red.svg)

Backend completo para um sistema de Food Truck, construído com Java e Spring Boot, seguindo princípios de Clean Architecture. O projeto é containerizado com Docker e inclui funcionalidades avançadas como processamento de pagamentos, sistema de fidelidade, e comunicação em tempo real.

---

## ✨ Funcionalidades Principais

- **Autenticação e Autorização:** Gerenciamento de usuários com JWT e papéis (Cliente, Funcionário, Admin).
- **Gerenciamento de Pedidos:** Criação, atualização de status e visualização de pedidos.
- **Cardápio Digital:** API para gerenciar itens do cardápio, incluindo categorias e tempo de preparo.
- **Processamento de Pagamentos:** Integração com Mercado Pago para pagamentos via PIX e Cartão de Crédito.
- **Sistema de Fidelidade:** Programa de pontos para clientes, com tiers (Bronze, Prata, Ouro, Diamante) e descontos.
- **Comunicação em Tempo Real:** Notificações de status de pedidos via WebSockets.
- **Mensageria Assíncrona:** Uso de RabbitMQ para processar eventos de novos pedidos e pontos de fidelidade de forma desacoplada.
- **Cache de Alta Performance:** Uso de Redis para cachear consultas frequentes (cardápio, usuários, etc.).
- **Relatórios e Métricas:** Endpoints para gerar relatórios de vendas, clientes e métricas de dashboard.
- **Documentação de API:** Geração automática de documentação com OpenAPI (Swagger).

---

## 🛠️ Arquitetura e Tecnologias

O projeto segue uma arquitetura limpa, dividida em três camadas principais:

- **`domain`**: Contém as entidades, regras de negócio e interfaces (ports). É o coração da aplicação, sem dependências de frameworks.
- **`application`**: Orquestra os casos de uso, conectando a camada de domínio com a de infraestrutura.
- **`infrastructure`**: Implementa as interfaces da camada de domínio. Contém controllers, repositórios JPA, gateways de pagamento, e configurações de frameworks.

### Stack de Tecnologias

- **Linguagem:** Java 21
- **Framework:** Spring Boot 3.5.5
- **Banco de Dados:** PostgreSQL (produção) / H2 (desenvolvimento)
- **Mensageria:** RabbitMQ
- **Cache:** Redis
- **Autenticação:** Spring Security + JWT
- **Containerização:** Docker e Docker Compose
- **Build:** Maven
- **Documentação:** OpenAPI (SpringDoc)

---

## 🚀 Como Executar o Projeto

### Pré-requisitos

- [Java 21 (JDK)](https://www.oracle.com/java/technologies/downloads/#java21)
- [Maven](https://maven.apache.org/download.cgi)
- [Docker](https://www.docker.com/get-started) e [Docker Compose](https://docs.docker.com/compose/install/)

### 1. Configuração do Ambiente

Clone o repositório e crie seu arquivo de configuração de ambiente a partir do exemplo fornecido.

```bash
# Copie o arquivo de exemplo
cp .env.example .env
```

Abra o arquivo `.env` e preencha as variáveis, especialmente as credenciais do Mercado Pago.

### 2. Iniciar a Infraestrutura com Docker

O `docker-compose.yml` irá configurar e iniciar todos os serviços necessários: PostgreSQL, RabbitMQ, Redis e pgAdmin.

```bash
docker-compose up -d
```

- **PostgreSQL:** Disponível em `localhost:5432`
- **RabbitMQ:** Disponível em `localhost:5672` (UI de gerenciamento em `http://localhost:15672`)
- **Redis:** Disponível em `localhost:6379`
- **pgAdmin:** Disponível em `http://localhost:5050`

### 3. Executar a Aplicação Spring Boot

Use o Maven Wrapper para compilar e executar a aplicação.

```bash
./mvnw spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`.

---

## 📚 API e Documentação

Com a aplicação em execução, a documentação completa da API (gerada pelo OpenAPI) pode ser acessada em:

- **Swagger UI:** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

Lá você encontrará todos os endpoints, modelos de dados e poderá testar a API diretamente.

---

## ⚙️ Configuração e Perfis

O projeto utiliza perfis do Spring para gerenciar diferentes configurações:

- **`development` (padrão):** Usa o banco de dados H2 em memória, habilita o H2 Console e logs detalhados.
- **`prod`:** Configurado para usar PostgreSQL, desabilita o H2 Console e otimiza o logging e as configurações de JPA para produção.

Para executar com o perfil de produção:

```bash
# Usando Maven
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

# Ou com o JAR empacotado
java -jar -Dspring.profiles.active=prod target/PortalChurras-0.0.1-SNAPSHOT.jar
```

---

## 🗂️ Estrutura de Diretórios

```
.
├── .env.example         # Exemplo de variáveis de ambiente
├── docker-compose.yml   # Orquestração dos serviços de infra
├── init.sql             # Script de inicialização do banco de dados
├── pom.xml              # Dependências e build do Maven
└── src
    └── main
        └── java
            └── com/exemple/apipagamento/portalchurras
                ├── application      # Casos de uso e serviços
                ├── domain           # Entidades e regras de negócio
                └── infrastructure   # Controllers, repositórios, gateways
```
