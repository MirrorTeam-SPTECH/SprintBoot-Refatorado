# 🔗 Checklist de Integração - Backend com Frontend e Banco

## 📊 1. CONFIGURAÇÃO DO BANCO DE DADOS (PostgreSQL)

### ✅ Variáveis de Ambiente Obrigatórias

```bash
# Banco de Dados
DB_URL=jdbc:postgresql://seu-host:5432/portalchurras
DB_USERNAME=seu_usuario
DB_PASSWORD=sua_senha_segura

# JWT (CRÍTICO - Mínimo 32 caracteres)
JWT_SECRET=sua-chave-secreta-super-segura-minimo-32-caracteres-change-in-production
JWT_EXPIRATION=3600000  # 1 hora em millisegundos

# Redis (Cache)
REDIS_HOST=seu-redis-host
REDIS_PORT=6379
REDIS_PASSWORD=sua-senha-redis  # opcional

# RabbitMQ (Mensageria)
RABBITMQ_HOST=seu-rabbitmq-host
RABBITMQ_PORT=5672
RABBITMQ_USER=guest
RABBITMQ_PASSWORD=guest

# Mercado Pago (Pagamentos)
MERCADO_PAGO_ACCESS_TOKEN=seu-access-token
MERCADO_PAGO_PUBLIC_KEY=sua-public-key
MERCADO_PAGO_CLIENT_ID=seu-client-id
MERCADO_PAGO_CLIENT_SECRET=seu-client-secret
MERCADO_PAGO_WEBHOOK_SECRET=seu-webhook-secret
PAYMENT_SUCCESS_URL=https://seu-site.com/success
PAYMENT_FAILURE_URL=https://seu-site.com/failure
PAYMENT_PENDING_URL=https://seu-site.com/pending
```

### 🗄️ Setup do Banco PostgreSQL

#### Opção 1: Docker (Desenvolvimento Local)

```bash
# Já está no docker-compose.yml - apenas execute:
docker-compose up -d postgres

# Acesso: localhost:5432
# Database: portalchurras
# User: postgres
# Password: postgres
```

#### Opção 2: AWS RDS (Produção)

1. Criar instância RDS PostgreSQL 15
2. Configurar Security Group (porta 5432)
3. Anotar endpoint: `xxx.rds.amazonaws.com:5432`
4. Criar database `portalchurras`
5. Usar as credenciais nos environment variables

#### Opção 3: PostgreSQL Local

Siga o guia: `SETUP_POSTGRES.md`

### 🔧 Executar Migrations (Script init.sql)

```bash
# O script init.sql já está no projeto
# Será executado automaticamente pelo Hibernate com:
spring.jpa.hibernate.ddl-auto=update  # Dev
spring.jpa.hibernate.ddl-auto=validate  # Prod
```

---

## 🌐 2. CONFIGURAÇÃO DO CORS (Frontend)

### ✅ Configurar URL do Frontend

No `application-prod.properties` ou via variável de ambiente:

```properties
# Permitir requisições do seu frontend (IP Elástico ou domínio)
CORS_ALLOWED_ORIGINS=http://seu-ip-elastico:porta,http://localhost:3000

# Para WebSocket
WEBSOCKET_ALLOWED_ORIGINS=http://seu-ip-elastico:porta,http://localhost:3000

# Exemplo com IP Elástico da AWS:
# CORS_ALLOWED_ORIGINS=http://54.123.45.67:3000,http://localhost:3000
```

### ⚠️ Importante: IPs Elásticos AWS

- **Frontend EC2**: Use o IP Elástico atribuído à instância
- **Backend EC2**: Use o IP Elástico atribuído à instância
- **Comunicação Interna**: Use IPs privados da VPC (mais rápido e seguro)
- **Sem HTTPS**: Se não tiver certificado SSL, use `http://` (não recomendado para produção)

### 📋 Endpoints Disponíveis para o Frontend

#### Autenticação

```
POST /api/auth/register  - Cadastro de usuário
POST /api/auth/login     - Login (retorna JWT token)
```

#### Menu

```
GET    /api/menu-items              - Listar itens ativos
GET    /api/menu-items/{id}         - Buscar por ID
GET    /api/menu-items/category/{category} - Listar por categoria
POST   /api/menu-items              - Criar item (ADMIN)
PUT    /api/menu-items/{id}         - Atualizar (ADMIN)
PATCH  /api/menu-items/{id}/deactivate - Desativar (ADMIN)
```

#### Pedidos

```
POST   /api/orders                  - Criar pedido
GET    /api/orders/{id}             - Buscar pedido
GET    /api/orders                  - Listar todos (EMPLOYEE/ADMIN)
GET    /api/orders/customer/{email} - Pedidos do cliente (ADMIN)
POST   /api/orders/{id}/items       - Adicionar item ao pedido
PATCH  /api/orders/{id}/items/{itemId} - Atualizar quantidade
DELETE /api/orders/{id}/items/{itemId} - Remover item
PATCH  /api/orders/{id}/status      - Atualizar status (EMPLOYEE/ADMIN)
PATCH  /api/orders/{id}/cancel      - Cancelar pedido
PATCH  /api/orders/{id}/notes       - Adicionar observações
```

#### Pagamentos

```
POST   /api/payments                - Criar pagamento
GET    /api/payments/{id}           - Buscar pagamento
GET    /api/payments/order/{orderId} - Pagamento do pedido
POST   /api/payments/{id}/pix       - Gerar QR Code PIX
GET    /api/payments/{id}/status    - Verificar status
```

#### Usuários

```
GET    /api/users/profile           - Perfil do usuário logado
PUT    /api/users/profile           - Atualizar perfil
PUT    /api/users/password          - Alterar senha
GET    /api/users                   - Listar usuários (ADMIN)
POST   /api/users                   - Criar usuário (ADMIN)
```

#### Fidelidade

```
GET    /api/loyalty/{userId}        - Programa de fidelidade
GET    /api/loyalty/{userId}/transactions - Histórico de pontos
POST   /api/loyalty/{userId}/redeem - Resgatar pontos
```

---

## 🔐 3. AUTENTICAÇÃO JWT (Frontend)

### ✅ Fluxo de Autenticação

#### 1. Login

```javascript
// Requisição
POST /api/auth/login
Content-Type: application/json

{
  "email": "usuario@email.com",
  "password": "senha123"
}

// Resposta
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "id": 1,
  "name": "Nome Usuario",
  "email": "usuario@email.com",
  "role": "CUSTOMER"
}
```

#### 2. Armazenar Token (Frontend)

```javascript
// Salvar no localStorage ou sessionStorage
localStorage.setItem("token", response.token);
localStorage.setItem("user", JSON.stringify(response));
```

#### 3. Usar Token nas Requisições

```javascript
// Axios
axios.defaults.headers.common["Authorization"] = `Bearer ${token}`;

// Fetch
fetch("/api/orders", {
  headers: {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  },
});
```

#### 4. Exemplo Completo (React/Axios)

```javascript
import axios from "axios";

// Configurar base URL com IP Elástico do backend
const api = axios.create({
  baseURL: "http://54.x.x.x:8080/api", // Substituir pelo seu IP
});

// Interceptor para adicionar token
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Interceptor para tratar erro 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Redirecionar para login
      localStorage.removeItem("token");
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);

export default api;
```

---

## 🚀 4. DEPLOY NA AWS EC2 COM IP ELÁSTICO

### 📋 Arquitetura Recomendada

```
┌─────────────────────────────────────────────┐
│           Internet Gateway                   │
└─────────────────┬───────────────────────────┘
                  │
┌─────────────────▼───────────────────────────┐
│  EC2 Frontend (IP Elástico: 54.x.x.1)      │
│  - React/Angular/Vue                        │
│  - Porta 80/3000                            │
└─────────────────┬───────────────────────────┘
                  │ (HTTP/WebSocket)
┌─────────────────▼───────────────────────────┐
│  EC2 Backend (IP Elástico: 54.x.x.2)       │
│  - Spring Boot                              │
│  - Porta 8080                               │
└─────────────────┬───────────────────────────┘
                  │ (JDBC/TCP)
┌─────────────────▼───────────────────────────┐
│  EC2 PostgreSQL (IP Privado: 10.x.x.3)     │
│  - PostgreSQL 15                            │
│  - Porta 5432 (somente VPC)                 │
└─────────────────────────────────────────────┘
```

### ✅ Passo a Passo - Deploy Backend EC2

#### 1. Criar Instância EC2

```bash
# Tipo: t2.micro ou t3.small
# AMI: Amazon Linux 2023 ou Ubuntu 22.04
# Key pair: Criar ou usar existente
# Security Group: Criar novo (portas abaixo)
```

#### 2. Configurar Security Group - Backend

```
Inbound Rules:
- SSH (22): Seu IP apenas
- Custom TCP (8080): IP Elástico do Frontend
- Custom TCP (8080): 0.0.0.0/0 (se precisar acessar diretamente)

Outbound Rules:
- All traffic: 0.0.0.0/0 (para acessar internet/banco)
```

#### 3. Associar IP Elástico

```bash
# No console AWS:
1. EC2 > Elastic IPs > Allocate Elastic IP address
2. Actions > Associate Elastic IP address
3. Selecionar sua instância Backend
4. Anotar o IP: 54.x.x.x
```

#### 4. Instalar Java 21 na EC2

```bash
# Conectar SSH
ssh -i sua-chave.pem ec2-user@54.x.x.x

# Amazon Linux 2023
sudo yum install java-21-amazon-corretto-headless -y
java -version  # Verificar instalação

# Ubuntu
sudo apt update
sudo apt install openjdk-21-jdk -y
```

#### 5. Transferir JAR para EC2

```bash
# No seu computador local (Windows PowerShell)
# 1. Gerar JAR
.\mvnw.cmd clean package -DskipTests

# 2. Copiar via SCP
scp -i sua-chave.pem target/PortalChurras-0.0.1-SNAPSHOT.jar ec2-user@54.x.x.x:/home/ec2-user/
```

#### 6. Configurar Variáveis de Ambiente

```bash
# Na EC2 via SSH
nano /home/ec2-user/app-config.sh

# Colar conteúdo:
export DB_URL="jdbc:postgresql://10.x.x.x:5432/portalchurras"
export DB_USERNAME="postgres"
export DB_PASSWORD="SenhaSegura123!"
export JWT_SECRET="sua-chave-jwt-super-segura-minimo-32-caracteres"
export JWT_EXPIRATION="3600000"
export REDIS_HOST="10.x.x.x"
export REDIS_PORT="6379"
export RABBITMQ_HOST="10.x.x.x"
export RABBITMQ_PORT="5672"
export RABBITMQ_USER="guest"
export RABBITMQ_PASSWORD="guest"
export CORS_ALLOWED_ORIGINS="http://54.x.x.1:3000,http://localhost:3000"
export WEBSOCKET_ALLOWED_ORIGINS="http://54.x.x.1:3000,http://localhost:3000"
export MERCADO_PAGO_ACCESS_TOKEN="seu-token"
export MERCADO_PAGO_PUBLIC_KEY="sua-public-key"

# Salvar: Ctrl+O, Enter, Ctrl+X
chmod +x /home/ec2-user/app-config.sh
```

#### 7. Criar Script de Inicialização

```bash
nano /home/ec2-user/start-backend.sh

# Colar:
#!/bin/bash
source /home/ec2-user/app-config.sh
cd /home/ec2-user
nohup java -jar PortalChurras-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  > backend.log 2>&1 &
echo $! > backend.pid
echo "Backend iniciado! PID: $(cat backend.pid)"

# Salvar e tornar executável
chmod +x /home/ec2-user/start-backend.sh
```

#### 8. Criar Script de Parada

```bash
nano /home/ec2-user/stop-backend.sh

# Colar:
#!/bin/bash
if [ -f backend.pid ]; then
  kill $(cat backend.pid)
  rm backend.pid
  echo "Backend parado!"
else
  echo "Backend não está rodando"
fi

chmod +x /home/ec2-user/stop-backend.sh
```

#### 9. Iniciar Aplicação

```bash
./start-backend.sh

# Verificar logs
tail -f backend.log

# Verificar se está rodando
curl localhost:8080/actuator/health
```

---

## 🎮 COMO RODAR A APLICAÇÃO (Resumo Rápido)

### 🏠 Desenvolvimento Local (Windows)

#### Opção 1: Via IDE (IntelliJ/Eclipse/VSCode)

```
1. Abrir o projeto na IDE
2. Configurar variáveis de ambiente no Run Configuration
3. Clicar em "Run" ou "Debug"
4. Acessar: http://localhost:8080
```

#### Opção 2: Via Maven Wrapper

```powershell
# No PowerShell do Windows
cd "c:\Users\aliss\BACK-END\4°Semestre\a\SprintBoot-Refatorado"

# Rodar com perfil de desenvolvimento (H2 database)
.\mvnw.cmd spring-boot:run

# Rodar com perfil de produção (PostgreSQL)
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod

# Acessar: http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui/index.html
```

#### Opção 3: Gerar e Rodar JAR

```powershell
# Gerar JAR
.\mvnw.cmd clean package -DskipTests

# Rodar JAR com H2 (desenvolvimento)
java -jar target/PortalChurras-0.0.1-SNAPSHOT.jar

# Rodar JAR com PostgreSQL (produção local)
java -jar target/PortalChurras-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

### ☁️ Produção AWS EC2

#### Primeira vez (Setup Inicial)

```bash
# 1. Conectar SSH
ssh -i sua-chave.pem ec2-user@54.x.x.x

# 2. Verificar se Java está instalado
java -version  # Deve mostrar Java 21

# 3. Verificar se JAR foi copiado
ls -lh /home/ec2-user/*.jar

# 4. Verificar variáveis de ambiente
cat /home/ec2-user/app-config.sh

# 5. Iniciar aplicação
./start-backend.sh

# 6. Aguardar ~30 segundos e verificar
curl localhost:8080/actuator/health
```

#### Dia a dia (Operações Comuns)

**Iniciar Backend:**

```bash
ssh -i sua-chave.pem ec2-user@54.x.x.x
./start-backend.sh
```

**Parar Backend:**

```bash
./stop-backend.sh
```

**Reiniciar Backend:**

```bash
./stop-backend.sh
./start-backend.sh
```

**Ver logs em tempo real:**

```bash
tail -f backend.log
```

**Ver últimas 100 linhas do log:**

```bash
tail -n 100 backend.log
```

**Verificar se está rodando:**

```bash
# Verificar processo
ps aux | grep java

# Verificar health
curl localhost:8080/actuator/health

# Verificar do seu computador (substituir IP)
curl http://54.x.x.x:8080/actuator/health
```

#### Usando Systemd (Recomendado para Produção)

**Iniciar:**

```bash
sudo systemctl start portal-churras
```

**Parar:**

```bash
sudo systemctl stop portal-churras
```

**Reiniciar:**

```bash
sudo systemctl restart portal-churras
```

**Ver status:**

```bash
sudo systemctl status portal-churras
```

**Ver logs:**

```bash
sudo journalctl -u portal-churras -f
```

**Habilitar auto-start (iniciar com o sistema):**

```bash
sudo systemctl enable portal-churras
```

### 🔄 Atualizar Aplicação na AWS

Quando fizer alterações no código:

```powershell
# 1. No Windows - Gerar novo JAR
.\mvnw.cmd clean package -DskipTests

# 2. Parar aplicação na EC2
ssh -i sua-chave.pem ec2-user@54.x.x.x "./stop-backend.sh"

# 3. Fazer backup do JAR antigo
ssh -i sua-chave.pem ec2-user@54.x.x.x "mv PortalChurras-0.0.1-SNAPSHOT.jar PortalChurras-backup.jar"

# 4. Copiar novo JAR
scp -i sua-chave.pem target/PortalChurras-0.0.1-SNAPSHOT.jar ec2-user@54.x.x.x:/home/ec2-user/

# 5. Iniciar novamente
ssh -i sua-chave.pem ec2-user@54.x.x.x "./start-backend.sh"

# 6. Verificar logs
ssh -i sua-chave.pem ec2-user@54.x.x.x "tail -f backend.log"
```

Ou se usar systemd:

```powershell
# 1. Gerar JAR
.\mvnw.cmd clean package -DskipTests

# 2. Parar serviço
ssh -i sua-chave.pem ec2-user@54.x.x.x "sudo systemctl stop portal-churras"

# 3. Copiar novo JAR
scp -i sua-chave.pem target/PortalChurras-0.0.1-SNAPSHOT.jar ec2-user@54.x.x.x:/home/ec2-user/

# 4. Iniciar serviço
ssh -i sua-chave.pem ec2-user@54.x.x.x "sudo systemctl start portal-churras"

# 5. Verificar status
ssh -i sua-chave.pem ec2-user@54.x.x.x "sudo systemctl status portal-churras"
```

### 📱 Verificar se está Funcionando

**Do seu computador:**

```bash
# Health check (substituir pelo seu IP)
curl http://54.x.x.x:8080/actuator/health

# Listar menu items
curl http://54.x.x.x:8080/api/menu-items

# Abrir Swagger no navegador
http://54.x.x.x:8080/swagger-ui/index.html
```

**No navegador:**

1. Abrir `http://54.x.x.x:8080/swagger-ui/index.html`
2. Testar endpoint de login
3. Copiar token JWT
4. Clicar em "Authorize" no Swagger
5. Testar outros endpoints

### 🐳 Docker Local (Alternativa)

Se preferir usar Docker localmente:

```powershell
# Iniciar todos os serviços (PostgreSQL, Redis, RabbitMQ)
docker-compose up -d

# Verificar serviços
docker-compose ps

# Rodar aplicação
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=prod

# Parar serviços
docker-compose down
```

---

## 🆘 COMANDOS ÚTEIS DE TROUBLESHOOTING

### Verificar Portas

```bash
# Ver processos na porta 8080
sudo lsof -i :8080
# ou
sudo netstat -tulpn | grep 8080
```

### Verificar Memória e CPU

```bash
# Uso de recursos
top
# ou mais amigável
htop

# Memória livre
free -h
```

### Verificar Conectividade

```bash
# Testar conexão com PostgreSQL
telnet 10.x.x.x 5432
# ou
nc -zv 10.x.x.x 5432

# Testar DNS
nslookup seu-dominio.com

# Verificar rota
traceroute 10.x.x.x
```

### Logs Detalhados

```bash
# Ver erros no log
grep -i error backend.log

# Ver warnings
grep -i warn backend.log

# Ver últimas exceções
grep -i exception backend.log | tail -n 50
```

---

## 🎯 FLUXO COMPLETO - PRIMEIRA VEZ

### 1️⃣ Setup Inicial Local

```powershell
# Clonar repositório (se ainda não tiver)
git clone https://github.com/MirrorTeam-SPTECH/SprintBoot-Refatorado.git
cd SprintBoot-Refatorado

# Testar localmente
.\mvnw.cmd clean test

# Rodar aplicação
.\mvnw.cmd spring-boot:run
```

### 2️⃣ Preparar AWS

```bash
# Criar instância EC2
# Associar IP Elástico
# Configurar Security Groups
# Instalar Java 21
```

### 3️⃣ Deploy

```powershell
# Gerar JAR
.\mvnw.cmd clean package -DskipTests

# Copiar para EC2
scp -i sua-chave.pem target/PortalChurras-0.0.1-SNAPSHOT.jar ec2-user@54.x.x.x:/home/ec2-user/

# Configurar ambiente
ssh -i sua-chave.pem ec2-user@54.x.x.x
nano app-config.sh  # Configurar variáveis

# Criar scripts de start/stop
nano start-backend.sh  # Criar script

# Iniciar
./start-backend.sh
```

### 4️⃣ Verificar

```bash
# Health check
curl http://54.x.x.x:8080/actuator/health

# Swagger
http://54.x.x.x:8080/swagger-ui/index.html
```

### 5️⃣ Conectar Frontend

```javascript
// Configurar URL no frontend
const API_URL = "http://54.x.x.x:8080/api";
```

**Pronto! Aplicação rodando! 🚀**

---

## 🎬 COMANDOS PARA COPIAR/COLAR (Quick Reference)

```bash
# === LOCAL (Windows) ===
.\mvnw.cmd spring-boot:run                    # Rodar dev
.\mvnw.cmd clean package -DskipTests          # Gerar JAR
java -jar target/*.jar                        # Rodar JAR

# === AWS EC2 ===
./start-backend.sh                            # Iniciar
./stop-backend.sh                             # Parar
tail -f backend.log                           # Ver logs
curl localhost:8080/actuator/health          # Verificar

# === SYSTEMD ===
sudo systemctl start portal-churras           # Iniciar
sudo systemctl stop portal-churras            # Parar
sudo systemctl restart portal-churras         # Reiniciar
sudo systemctl status portal-churras          # Status
sudo journalctl -u portal-churras -f          # Logs

# === DOCKER ===
docker-compose up -d                          # Iniciar serviços
docker-compose down                           # Parar
docker-compose ps                             # Ver status
```

#### 10. Configurar Systemd (Auto-start)

```bash
sudo nano /etc/systemd/system/portal-churras.service

# Colar:
[Unit]
Description=Portal Churras Backend
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/home/ec2-user
EnvironmentFile=/home/ec2-user/app-config.sh
ExecStart=/usr/bin/java -jar /home/ec2-user/PortalChurras-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target

# Habilitar e iniciar
sudo systemctl daemon-reload
sudo systemctl enable portal-churras
sudo systemctl start portal-churras
sudo systemctl status portal-churras
```

### 🔒 Security Groups AWS (Configuração Correta)

#### Backend EC2

```
Inbound:
- Type: SSH, Port: 22, Source: Seu IP
- Type: Custom TCP, Port: 8080, Source: Frontend Security Group
- Type: Custom TCP, Port: 8080, Source: 0.0.0.0/0 (opcional, para testes)

Outbound:
- All traffic
```

#### PostgreSQL EC2 (se separado)

```
Inbound:
- Type: PostgreSQL, Port: 5432, Source: Backend Security Group
- Type: SSH, Port: 22, Source: Seu IP

Outbound:
- All traffic
```

#### Frontend EC2

```
Inbound:
- Type: HTTP, Port: 80, Source: 0.0.0.0/0
- Type: Custom TCP, Port: 3000, Source: 0.0.0.0/0
- Type: SSH, Port: 22, Source: Seu IP

Outbound:
- All traffic
```

### 📝 Comunicação Entre Instâncias

#### Usar IPs Privados (Mais Rápido e Seguro)

```bash
# Backend conecta ao PostgreSQL via IP privado
DB_URL=jdbc:postgresql://10.0.1.50:5432/portalchurras

# Frontend conecta ao Backend via IP Elástico PÚBLICO
BACKEND_URL=http://54.x.x.x:8080
```

#### Configuração CORS no Backend

```properties
# Use o IP Elástico do Frontend
CORS_ALLOWED_ORIGINS=http://54.123.45.67:3000
```

---

## 📡 5. WEBSOCKET (Notificações em Tempo Real)

### ✅ Frontend - Conectar ao WebSocket

```javascript
// Usando SockJS + Stomp
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

// Substituir pelo IP Elástico do seu backend
const socket = new SockJS("http://54.x.x.x:8080/ws");
const stompClient = new Client({
  webSocketFactory: () => socket,
  onConnect: () => {
    console.log("Connected to WebSocket");

    // Inscrever em tópicos
    stompClient.subscribe("/topic/orders", (message) => {
      const order = JSON.parse(message.body);
      console.log("Novo pedido:", order);
    });

    stompClient.subscribe("/topic/payments", (message) => {
      const payment = JSON.parse(message.body);
      console.log("Status pagamento:", payment);
    });
  },
});

stompClient.activate();
```

---

## ✅ 6. CHECKLIST FINAL PRÉ-PRODUÇÃO

### Banco de Dados

- [ ] PostgreSQL rodando e acessível
- [ ] Database `portalchurras` criada
- [ ] Tabelas criadas (via Hibernate)
- [ ] Backup configurado
- [ ] Índices criados para performance

### Variáveis de Ambiente

- [ ] JWT_SECRET configurado (32+ caracteres)
- [ ] DB_URL, DB_USERNAME, DB_PASSWORD configurados
- [ ] REDIS_HOST configurado (ou desabilitado)
- [ ] RABBITMQ_HOST configurado (ou desabilitado)
- [ ] MERCADO_PAGO credentials configuradas
- [ ] CORS_ALLOWED_ORIGINS com URL do frontend

### Segurança

- [ ] IP Elástico associado às instâncias EC2
- [ ] Security Groups configurados corretamente
- [ ] SSH apenas do seu IP
- [ ] Comunicação interna via IPs privados (VPC)
- [ ] Senhas fortes em produção
- [ ] JWT expiration adequado (1 hora recomendado)
- [ ] Logs configurados (não exibir dados sensíveis)
- [ ] HTTPS habilitado (certificado SSL) - **Recomendado para produção**

### Frontend

- [ ] URL do backend configurada (IP Elástico + porta)
- [ ] CORS funcionando (testar requisições)
- [ ] Autenticação JWT funcionando
- [ ] WebSocket conectando
- [ ] Tratamento de erros 401/403

### Testes

- [ ] Testar login/cadastro
- [ ] Testar criação de pedido
- [ ] Testar pagamento PIX
- [ ] Testar notificações WebSocket
- [ ] Testar em diferentes navegadores

---

## 📞 TESTE DE CONECTIVIDADE

### Backend Health Check

```bash
# Verificar se backend está respondendo (substituir pelo seu IP Elástico)
curl http://54.x.x.x:8080/actuator/health

# Resposta esperada:
{"status":"UP"}
```

### Teste de Login

```bash
# Substituir pelo seu IP Elástico
curl -X POST http://54.x.x.x:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"senha123"}'
```

### Teste CORS

```javascript
// No console do navegador (frontend)
// Substituir pelo seu IP Elástico do backend
fetch("http://54.x.x.x:8080/api/menu-items")
  .then((r) => r.json())
  .then(console.log)
  .catch(console.error);
```

### Verificar Conectividade Entre EC2s

```bash
# Do Frontend para Backend (via IP Privado - mais rápido)
curl http://10.x.x.x:8080/actuator/health

# Do Backend para PostgreSQL
psql -h 10.x.x.x -U postgres -d portalchurras -c "SELECT version();"
```

---

## 🆘 TROUBLESHOOTING COMUM

### ❌ Erro CORS

**Sintoma:** `Access-Control-Allow-Origin header is missing`
**Solução:** Configurar `CORS_ALLOWED_ORIGINS` com URL do frontend

### ❌ Erro 401 Unauthorized

**Sintoma:** Todas requisições retornam 401
**Solução:**

- Verificar se token está sendo enviado
- Verificar se JWT_SECRET é o mesmo em todas instâncias
- Token pode ter expirado

### ❌ Erro de Conexão com Banco

**Sintoma:** `Connection refused` ou `Cannot connect to database`
**Solução:**

- Verificar DB_URL, DB_USERNAME, DB_PASSWORD
- Verificar se PostgreSQL está rodando
- Verificar Security Group/Firewall (porta 5432)

### ❌ WebSocket não conecta

**Sintoma:** `WebSocket connection failed`
**Solução:**

- Verificar WEBSOCKET_ALLOWED_ORIGINS
- Verificar se o servidor permite WebSocket
- Usar protocolo correto (ws:// ou wss://)

---

## 📚 DOCUMENTAÇÃO ADICIONAL

- **Swagger UI:** `http://seu-ip-elastico:8080/swagger-ui/index.html`
- **API Docs:** `http://seu-ip-elastico:8080/v3/api-docs`
- **Health Check:** `http://seu-ip-elastico:8080/actuator/health`
- **Metrics:** `http://seu-ip-elastico:8080/actuator/metrics`

**Exemplo com IP:** `http://54.123.45.67:8080/swagger-ui/index.html`

---

## 🎯 PRÓXIMOS PASSOS

1. ✅ Configurar banco de dados
2. ✅ Configurar variáveis de ambiente
3. ✅ Fazer deploy do backend na AWS
4. ✅ Configurar CORS com URL do frontend
5. ✅ Integrar autenticação no frontend
6. ✅ Testar endpoints da API
7. ✅ Configurar WebSocket
8. ✅ Testar fluxo completo
9. ✅ Monitorar logs e métricas

**Boa sorte com a integração! 🚀**
