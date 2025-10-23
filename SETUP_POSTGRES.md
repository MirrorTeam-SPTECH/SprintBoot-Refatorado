# 🚀 Guia de Configuração - PostgreSQL + Frontend

## 📋 PRÉ-REQUISITOS

### 1. PostgreSQL Instalado e Rodando
```bash
# Verificar se está rodando
psql --version

# Iniciar serviço (Windows)
net start postgresql-x64-15
```

### 2. Banco de Dados Criado
```sql
-- No pgAdmin ou psql:
CREATE DATABASE portalchurras;
```

---

## 🗄️ CONFIGURAÇÃO DO BACKEND (PostgreSQL)

### Passo 1: Criar application-prod.properties

Crie o arquivo: `src/main/resources/application-prod.properties`

```properties
# ============================================
# PostgreSQL Configuration (Production)
# ============================================
spring.datasource.url=jdbc:postgresql://localhost:5432/portalchurras
spring.datasource.username=postgres
spring.datasource.password=sua_senha_aqui
spring.datasource.driver-class-name=org.postgresql.Driver

# ============================================
# JPA/Hibernate Configuration
# ============================================
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true

# ============================================
# Connection Pool (HikariCP)
# ============================================
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000

# ============================================
# JWT Configuration
# ============================================
jwt.secret=${JWT_SECRET:your-256-bit-secret-key-here-change-in-production}
jwt.expiration=86400000

# ============================================
# Redis Configuration
# ============================================
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.cache.type=redis

# ============================================
# RabbitMQ Configuration
# ============================================
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=guest
spring.rabbitmq.password=guest
spring.rabbitmq.enabled=true

# Custom RabbitMQ Properties
rabbitmq.exchange.orders=orders-exchange
rabbitmq.queue.new-orders=new-orders-queue
rabbitmq.queue.order-status=order-status-queue
rabbitmq.queue.notifications=notifications-queue
rabbitmq.queue.loyalty-points=loyalty-points-queue
rabbitmq.routing-key.new-order=order.new
rabbitmq.routing-key.order-status=order.status
rabbitmq.routing-key.notification=notification.send
rabbitmq.routing-key.loyalty=loyalty.update

# ============================================
# Mercado Pago Configuration
# ============================================
mercado-pago.access-token=${MERCADO_PAGO_ACCESS_TOKEN}
mercado-pago.public-key=${MERCADO_PAGO_PUBLIC_KEY}
mercado-pago.client-id=${MERCADO_PAGO_CLIENT_ID}

# ============================================
# Server Configuration
# ============================================
server.port=8080
server.compression.enabled=true
server.compression.mime-types=application/json,application/xml,text/html,text/xml,text/plain
```

### Passo 2: Executar com Perfil de Produção

```bash
# Método 1: Via Maven
./mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod

# Método 2: Via JAR compilado
./mvnw.cmd clean package -DskipTests
java -jar -Dspring.profiles.active=prod target/PortalChurras-0.0.1-SNAPSHOT.jar

# Método 3: Variável de ambiente
set SPRING_PROFILES_ACTIVE=prod
./mvnw.cmd spring-boot:run
```

### Passo 3: Verificar Conexão

Após iniciar, você verá no log:
```
2025-10-23 ... : HikariPool-1 - Starting...
2025-10-23 ... : HikariPool-1 - Start completed.
2025-10-23 ... : Hibernate: create table if not exists users ...
```

---

## 🌐 CONFIGURAÇÃO DO FRONTEND (CORS)

### Passo 1: Verificar SecurityConfig

O arquivo `SecurityConfig.java` já está configurado com CORS. Verifique se está assim:

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList(
        "http://localhost:3000",  // React/Next.js
        "http://localhost:4200",  // Angular
        "http://localhost:8081",  // Vue
        "http://localhost:5173"   // Vite
    ));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### Passo 2: Configurar Frontend

#### Para React/Next.js:
```javascript
// api.js ou axios.config.js
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Interceptor para adicionar token JWT
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default api;
```

#### Exemplo de Uso:
```javascript
// Login
const login = async (email, password) => {
  const response = await api.post('/auth/login', { email, password });
  localStorage.setItem('token', response.data.token);
  return response.data;
};

// Buscar menu
const getMenuItems = async () => {
  const response = await api.get('/menu-items');
  return response.data;
};

// Criar pedido (requer autenticação)
const createOrder = async (orderData) => {
  const response = await api.post('/orders', orderData);
  return response.data;
};
```

---

## 📡 ENDPOINTS DISPONÍVEIS

### Autenticação
- `POST /api/auth/login` - Login
- `POST /api/auth/refresh` - Refresh token
- `POST /api/auth/validate` - Validar token

### Usuários
- `POST /api/users/register` - Registrar cliente
- `GET /api/users/me` - Perfil do usuário logado
- `PUT /api/users/me` - Atualizar perfil
- `PUT /api/users/me/password` - Trocar senha

### Menu
- `GET /api/menu-items` - Listar itens ativos
- `GET /api/menu-items/{id}` - Buscar item por ID
- `GET /api/menu-items/category/{category}` - Listar por categoria
- `POST /api/menu-items` - Criar item (ADMIN)
- `PUT /api/menu-items/{id}` - Atualizar item (ADMIN)
- `DELETE /api/menu-items/{id}/deactivate` - Desativar item (ADMIN)

### Pedidos
- `POST /api/orders` - Criar pedido
- `GET /api/orders` - Listar pedidos do usuário
- `GET /api/orders/{id}` - Buscar pedido por ID
- `PUT /api/orders/{id}/status` - Atualizar status (EMPLOYEE/ADMIN)
- `DELETE /api/orders/{id}/cancel` - Cancelar pedido

### Pagamentos
- `POST /api/payments/{orderId}/process` - Processar pagamento
- `GET /api/payments/order/{orderId}` - Buscar pagamento por pedido
- `GET /api/payments/{id}/status` - Verificar status do pagamento

### Fidelidade
- `GET /api/loyalty/balance` - Consultar saldo de pontos
- `GET /api/loyalty/history` - Histórico de pontos

---

## 🔐 VARIÁVEIS DE AMBIENTE (Produção)

Crie um arquivo `.env` ou configure no sistema:

```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/portalchurras
DB_USERNAME=postgres
DB_PASSWORD=sua_senha_segura

# JWT
JWT_SECRET=sua-chave-secreta-256-bits-aqui-troque-em-producao

# Mercado Pago
MERCADO_PAGO_ACCESS_TOKEN=seu_token_aqui
MERCADO_PAGO_PUBLIC_KEY=sua_chave_publica
MERCADO_PAGO_CLIENT_ID=seu_client_id

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
```

---

## 🐳 DOCKER (Opcional - Desenvolvimento Local)

### docker-compose.yml
```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: portalchurras-db
    environment:
      POSTGRES_DB: portalchurras
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    container_name: portalchurras-redis
    ports:
      - "6379:6379"

  rabbitmq:
    image: rabbitmq:3-management-alpine
    container_name: portalchurras-rabbitmq
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    ports:
      - "5672:5672"
      - "15672:15672"

volumes:
  postgres_data:
```

### Iniciar serviços:
```bash
docker-compose up -d
```

---

## ✅ CHECKLIST DE DEPLOY

### Backend
- [ ] PostgreSQL configurado e rodando
- [ ] Banco de dados criado
- [ ] `application-prod.properties` configurado
- [ ] Variáveis de ambiente configuradas
- [ ] Redis rodando (opcional)
- [ ] RabbitMQ rodando (opcional)
- [ ] Build executado sem erros: `./mvnw.cmd clean package`
- [ ] Aplicação iniciada com perfil prod: `java -jar -Dspring.profiles.active=prod target/*.jar`

### Frontend
- [ ] Axios/Fetch configurado com baseURL correto
- [ ] Interceptor de token implementado
- [ ] CORS configurado no backend
- [ ] Tratamento de erros HTTP implementado
- [ ] Loading states implementados

### Testes
- [ ] Testar login/registro
- [ ] Testar busca de itens do menu
- [ ] Testar criação de pedido
- [ ] Testar pagamento
- [ ] Verificar logs de erro

---

## 🆘 TROUBLESHOOTING

### Erro: "Connection refused" ao PostgreSQL
```bash
# Verificar se PostgreSQL está rodando
net start postgresql-x64-15

# Verificar porta
netstat -an | findstr :5432
```

### Erro: CORS
- Verificar se o frontend está na lista de origens permitidas
- Verificar se o cabeçalho `Authorization` está sendo enviado
- Testar com Postman primeiro (não tem CORS)

### Erro: "JWT token expired"
- Implementar refresh token no frontend
- Armazenar token de forma segura (HttpOnly cookies ou localStorage)

### Erro 403 Forbidden
- Verificar se o token está sendo enviado: `Authorization: Bearer {token}`
- Verificar se o usuário tem a role correta (ADMIN, EMPLOYEE, CUSTOMER)
- Verificar se o endpoint requer autenticação

---

## 📚 DOCUMENTAÇÃO DA API

Acesse o Swagger UI:
```
http://localhost:8080/swagger-ui.html
```

Ou OpenAPI JSON:
```
http://localhost:8080/v3/api-docs
```

---

**Feito! 🎉** Sua aplicação está pronta para integração com o frontend!
