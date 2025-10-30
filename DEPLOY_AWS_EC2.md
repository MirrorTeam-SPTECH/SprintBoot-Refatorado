# 🚀 Deploy na AWS EC2 - Portal Churras Backend

## 📋 **Arquitetura AWS:**

```
Frontend:  3.94.191.149 (IP Público)
Backend 1: 10.0.2.11 (IP Privado) ← Este servidor
Backend 2: 10.0.2.10 (IP Privado) ← Outro servidor
Database:  10.0.2.12 (IP Privado)
```

---

## 🔧 **1. Configurar a EC2 do Backend (10.0.2.11 ou 10.0.2.10)**

### **1.1 Conectar na EC2:**
```bash
ssh -i sua-chave.pem ubuntu@IP_PUBLICO_DA_EC2
```

### **1.2 Enviar arquivos necessários:**
```powershell
# No Windows (PowerShell)
scp -i sua-chave.pem .env.production ubuntu@IP_PUBLICO:/home/ubuntu/
scp -i sua-chave.pem nginx-config.conf ubuntu@IP_PUBLICO:/home/ubuntu/
scp -i sua-chave.pem setup-ec2.sh ubuntu@IP_PUBLICO:/home/ubuntu/
scp -i sua-chave.pem init.sql ubuntu@IP_PUBLICO:/home/ubuntu/
```

### **1.3 Executar script de instalação:**
```bash
chmod +x setup-ec2.sh
sudo ./setup-ec2.sh
```

---

## 🐘 **2. Configurar PostgreSQL (10.0.2.12)**

**⚠️ IMPORTANTE:** O PostgreSQL está em **OUTRA** instância (10.0.2.12).

### **2.1 Conectar na instância do banco:**
```bash
ssh -i sua-chave.pem ubuntu@IP_PUBLICO_DO_BANCO
```

### **2.2 Configurar PostgreSQL para aceitar conexões remotas:**

```bash
# Editar postgresql.conf
sudo nano /etc/postgresql/15/main/postgresql.conf
```

**Alterar:**
```
listen_addresses = '*'  # Aceita conexões de qualquer IP
```

**Editar pg_hba.conf:**
```bash
sudo nano /etc/postgresql/15/main/pg_hba.conf
```

**Adicionar no final:**
```
# Permitir conexões das instâncias do backend
host    portal_churras    postgres    10.0.2.11/32    md5
host    portal_churras    postgres    10.0.2.10/32    md5
```

**Reiniciar PostgreSQL:**
```bash
sudo systemctl restart postgresql
```

### **2.3 Criar banco e importar dados:**
```bash
sudo -u postgres psql -c "CREATE DATABASE portal_churras;"
sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'SenhaSegura@2025';"
sudo -u postgres psql -d portal_churras -f init.sql
```

---

## 🌐 **3. Configurar Nginx (em 10.0.2.11 e 10.0.2.10)**

### **3.1 Remover configuração padrão:**
```bash
sudo rm /etc/nginx/sites-enabled/default
```

### **3.2 Criar nova configuração:**
```bash
sudo cp nginx-config.conf /etc/nginx/sites-available/portalchurras
```

### **3.3 Habilitar site:**
```bash
sudo ln -s /etc/nginx/sites-available/portalchurras /etc/nginx/sites-enabled/
```

### **3.4 Testar e reiniciar:**
```bash
sudo nginx -t
sudo systemctl restart nginx
sudo systemctl status nginx
```

---

## ☕ **4. Deploy da Aplicação Spring Boot**

### **4.1 Fazer build no Windows:**
```powershell
./mvnw clean package -DskipTests
```

### **4.2 Enviar JAR para EC2:**
```powershell
scp -i sua-chave.pem target/PortalChurras-0.0.1-SNAPSHOT.jar ubuntu@IP_PUBLICO:/opt/portalchurras/portalchurras.jar
```

### **4.3 Copiar arquivo .env de produção:**
```bash
# Na EC2
sudo cp .env.production /opt/portalchurras/.env
```

### **4.4 Iniciar aplicação:**
```bash
sudo systemctl start portalchurras
sudo systemctl enable portalchurras
```

### **4.5 Ver logs:**
```bash
sudo journalctl -u portalchurras -f
```

---

## 🔥 **5. Configurar Security Groups na AWS**

### **Backend (10.0.2.11 e 10.0.2.10):**

| Porta | Protocolo | Origem | Descrição |
|-------|-----------|--------|-----------|
| 80 | TCP | Security Group do Frontend | Nginx |
| 8080 | TCP | localhost | Spring Boot (interno) |
| 22 | TCP | Seu IP | SSH |
| 5672 | TCP | localhost | RabbitMQ (interno) |
| 6379 | TCP | localhost | Redis (interno) |

### **Database (10.0.2.12):**

| Porta | Protocolo | Origem | Descrição |
|-------|-----------|--------|-----------|
| 5432 | TCP | Security Group do Backend | PostgreSQL |
| 22 | TCP | Seu IP | SSH |

### **Frontend (3.94.191.149):**

| Porta | Protocolo | Origem | Descrição |
|-------|-----------|--------|-----------|
| 80 | TCP | 0.0.0.0/0 | HTTP público |
| 443 | TCP | 0.0.0.0/0 | HTTPS (quando configurar SSL) |
| 22 | TCP | Seu IP | SSH |

---

## ✅ **6. Verificar Instalação**

### **6.1 Testar backend diretamente:**
```bash
curl http://localhost:8080/actuator/health
```

**Esperado:**
```json
{"status":"UP"}
```

### **6.2 Testar através do Nginx:**
```bash
curl http://10.0.2.11/actuator/health
```

### **6.3 Testar do frontend:**
```bash
# Na máquina do frontend
curl http://10.0.2.11/actuator/health
curl http://10.0.2.10/actuator/health
```

### **6.4 Testar conexão com banco:**
```bash
# Na EC2 do backend
psql -h 10.0.2.12 -U postgres -d portal_churras -c "SELECT version();"
```

---

## 🔄 **7. Load Balancing (Opcional)**

Se quiser balancear entre as duas instâncias do backend, configure um **Application Load Balancer** na AWS:

1. **Criar ALB** no console AWS
2. **Target Group** com 10.0.2.11:80 e 10.0.2.10:80
3. **Health Check** em `/actuator/health`
4. **Atualizar CORS** no backend com o DNS do ALB

---

## 🐛 **8. Troubleshooting**

### **Erro: Connection refused (banco)**
```bash
# Verificar se o PostgreSQL está aceitando conexões
sudo netstat -plnt | grep 5432

# Verificar logs do PostgreSQL
sudo tail -f /var/log/postgresql/postgresql-15-main.log
```

### **Erro: RabbitMQ não conecta**
```bash
# Verificar status
sudo systemctl status rabbitmq-server

# Ver logs
sudo journalctl -u rabbitmq-server -f
```

### **Erro: Nginx 502 Bad Gateway**
```bash
# Verificar se Spring Boot está rodando
sudo systemctl status portalchurras

# Ver logs da aplicação
sudo journalctl -u portalchurras -f

# Ver logs do Nginx
sudo tail -f /var/log/nginx/portalchurras-error.log
```

### **Erro: CORS**
```bash
# Verificar se o IP do frontend está correto no .env
cat /opt/portalchurras/.env | grep CORS

# Testar CORS
curl -H "Origin: http://3.94.191.149" \
     -H "Access-Control-Request-Method: POST" \
     -X OPTIONS \
     http://10.0.2.11/api/auth/login -v
```

---

## 📊 **9. Monitoramento**

### **Ver logs em tempo real:**
```bash
# Aplicação
sudo journalctl -u portalchurras -f

# Nginx
sudo tail -f /var/log/nginx/portalchurras-access.log

# PostgreSQL (na instância 10.0.2.12)
sudo tail -f /var/log/postgresql/postgresql-15-main.log
```

### **Verificar recursos:**
```bash
# CPU e Memória
htop

# Espaço em disco
df -h

# Conexões ativas
sudo netstat -an | grep :8080 | wc -l
```

---

## 🔒 **10. Segurança**

### **10.1 Alterar senhas padrão:**
```bash
# PostgreSQL
sudo -u postgres psql -c "ALTER USER postgres WITH PASSWORD 'NovaSenhaForte@2025';"

# RabbitMQ
sudo rabbitmqctl change_password admin 'NovaSenhaRabbitMQ@2025'

# Atualizar .env
sudo nano /opt/portalchurras/.env
```

### **10.2 Firewall:**
```bash
# Bloquear portas desnecessárias
sudo ufw enable
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow from 10.0.2.12 to any port 5432
```

---

## 🎯 **11. Endpoints para Teste**

Depois de tudo configurado, teste:

```bash
# Health check
curl http://10.0.2.11/actuator/health

# Login
curl -X POST http://10.0.2.11/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@churras.com","password":"Admin@123"}'

# Cardápio (público)
curl http://10.0.2.11/api/menu-items
```

---

**✅ Deploy completo! Agora o backend está pronto para integrar com o frontend em 3.94.191.149** 🚀
