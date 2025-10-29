# 🧪 Guia de Testes no Postman - Portal Churras API

## 🚀 Passo 1: Iniciar a Aplicação

```bash
./mvnw spring-boot:run
```

Aguarde até aparecer: `Tomcat started on port 8080 (http)`

---

## 📋 Base URL

```
http://localhost:8080
```

---

## 🔐 1. AUTENTICAÇÃO (Login)

### **POST** `/api/auth/login`

**Body (JSON):**
```json
{
  "email": "admin@churras.com",
  "password": "Admin@123"
}
```

**Resposta Esperada:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "user": {
    "id": 1,
    "name": "Admin",
    "email": "admin@churras.com",
    "role": "ADMIN"
  }
}
```

**⚠️ IMPORTANTE:** Copie o `token` retornado! Você vai usar em todas as outras requisições.

---

## 🔑 2. Configurar Headers para Requisições Autenticadas

Para todas as requisições abaixo, adicione no **Headers**:

```
Authorization: Bearer SEU_TOKEN_AQUI
```

No Postman:
1. Vá na aba **Headers**
2. Adicione:
   - **Key:** `Authorization`
   - **Value:** `Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...` (cole o token completo)

---

## 👥 3. USUÁRIOS

### **GET** `/api/users` - Listar todos os usuários
- **Headers:** `Authorization: Bearer {token}`
- **Resposta:** Lista com 3 usuários (admin, cliente, garçom)

### **GET** `/api/users/{id}` - Buscar usuário por ID
- **Exemplo:** `http://localhost:8080/api/users/1`
- **Headers:** `Authorization: Bearer {token}`

### **POST** `/api/users` - Criar novo usuário
- **Headers:** `Authorization: Bearer {token}`
- **Body (JSON):**
```json
{
  "name": "Novo Cliente",
  "email": "cliente2@exemplo.com",
  "password": "Senha@123",
  "role": "CUSTOMER",
  "phone": "11987654321"
}
```

### **PUT** `/api/users/{id}` - Atualizar usuário
- **Exemplo:** `http://localhost:8080/api/users/1`
- **Headers:** `Authorization: Bearer {token}`
- **Body (JSON):**
```json
{
  "name": "Admin Atualizado",
  "email": "admin@churras.com",
  "phone": "11999999999"
}
```

### **DELETE** `/api/users/{id}` - Deletar usuário (soft delete)
- **Exemplo:** `http://localhost:8080/api/users/3`
- **Headers:** `Authorization: Bearer {token}`

---

## 🍔 4. CARDÁPIO (Menu Items)

### **GET** `/api/menu-items` - Listar todos os itens do cardápio
- **Sem autenticação** (público)
- **Resposta:** 27 itens em 6 categorias

### **GET** `/api/menu-items/{id}` - Buscar item por ID
- **Exemplo:** `http://localhost:8080/api/menu-items/1`

### **GET** `/api/menu-items/category/{category}` - Buscar por categoria
- **Exemplo:** `http://localhost:8080/api/menu-items/category/HAMBURGUERES`
- **Categorias disponíveis:**
  - `COMBOS`
  - `HAMBURGUERES`
  - `ESPETINHOS`
  - `PORCOES`
  - `BEBIDAS`
  - `ADICIONAIS`

### **POST** `/api/menu-items` - Criar novo item (ADMIN)
- **Headers:** `Authorization: Bearer {token}`
- **Body (JSON):**
```json
{
  "name": "X-Picanha",
  "description": "Hambúrguer de picanha com queijo brie",
  "price": 35.90,
  "category": "HAMBURGUERES",
  "preparationTime": "20 min",
  "active": true
}
```

### **PUT** `/api/menu-items/{id}` - Atualizar item (ADMIN)
- **Exemplo:** `http://localhost:8080/api/menu-items/1`
- **Headers:** `Authorization: Bearer {token}`
- **Body (JSON):**
```json
{
  "name": "X-Burger Premium",
  "description": "Hambúrguer premium com ingredientes selecionados",
  "price": 22.90,
  "category": "HAMBURGUERES",
  "preparationTime": "15 min",
  "active": true
}
```

### **DELETE** `/api/menu-items/{id}` - Deletar item (ADMIN)
- **Exemplo:** `http://localhost:8080/api/menu-items/1`
- **Headers:** `Authorization: Bearer {token}`

---

## 🛒 5. PEDIDOS (Orders)

### **POST** `/api/orders` - Criar novo pedido
- **Headers:** `Authorization: Bearer {token}`
- **Body (JSON):**
```json
{
  "customerId": 2,
  "items": [
    {
      "menuItemId": 4,
      "quantity": 2,
      "observations": "Sem cebola"
    },
    {
      "menuItemId": 15,
      "quantity": 1
    }
  ],
  "notes": "Entrega rápida por favor"
}
```

### **GET** `/api/orders` - Listar todos os pedidos
- **Headers:** `Authorization: Bearer {token}`

### **GET** `/api/orders/{id}` - Buscar pedido por ID
- **Exemplo:** `http://localhost:8080/api/orders/1`
- **Headers:** `Authorization: Bearer {token}`

### **GET** `/api/orders/customer/{customerId}` - Pedidos de um cliente
- **Exemplo:** `http://localhost:8080/api/orders/customer/2`
- **Headers:** `Authorization: Bearer {token}`

### **PUT** `/api/orders/{id}/status` - Atualizar status do pedido
- **Exemplo:** `http://localhost:8080/api/orders/1/status?status=CONFIRMED`
- **Headers:** `Authorization: Bearer {token}`
- **Query Params:**
  - `status` = `PENDING` | `CONFIRMED` | `IN_PREPARATION` | `READY` | `DELIVERED` | `CANCELLED`

### **DELETE** `/api/orders/{id}` - Cancelar pedido
- **Exemplo:** `http://localhost:8080/api/orders/1`
- **Headers:** `Authorization: Bearer {token}`

---

## 💳 6. PAGAMENTOS (Payments)

### **POST** `/api/payments` - Criar pagamento
- **Headers:** `Authorization: Bearer {token}`
- **Body (JSON):**
```json
{
  "orderId": 1,
  "method": "PIX",
  "amount": 54.80
}
```

**Métodos de pagamento:**
- `CREDIT_CARD`
- `DEBIT_CARD`
- `PIX`
- `BOLETO`
- `CASH`

### **GET** `/api/payments/{id}` - Buscar pagamento por ID
- **Exemplo:** `http://localhost:8080/api/payments/1`
- **Headers:** `Authorization: Bearer {token}`

### **GET** `/api/payments/order/{orderId}` - Pagamento de um pedido
- **Exemplo:** `http://localhost:8080/api/payments/order/1`
- **Headers:** `Authorization: Bearer {token}`

### **PUT** `/api/payments/{id}/status` - Atualizar status do pagamento
- **Exemplo:** `http://localhost:8080/api/payments/1/status?status=APPROVED`
- **Headers:** `Authorization: Bearer {token}`
- **Query Params:**
  - `status` = `PENDING` | `PROCESSING` | `APPROVED` | `REJECTED` | `CANCELLED` | `EXPIRED` | `REFUNDED`

---

## 🎁 7. PROGRAMA DE FIDELIDADE (Loyalty)

### **GET** `/api/loyalty/{userId}` - Buscar programa de fidelidade do usuário
- **Exemplo:** `http://localhost:8080/api/loyalty/2`
- **Headers:** `Authorization: Bearer {token}`

### **GET** `/api/loyalty/{userId}/transactions` - Histórico de transações
- **Exemplo:** `http://localhost:8080/api/loyalty/2/transactions`
- **Headers:** `Authorization: Bearer {token}`

### **POST** `/api/loyalty/{userId}/redeem` - Resgatar pontos
- **Exemplo:** `http://localhost:8080/api/loyalty/2/redeem?points=100`
- **Headers:** `Authorization: Bearer {token}`
- **Query Params:**
  - `points` = quantidade de pontos a resgatar

---

## 📊 8. HEALTH CHECK

### **GET** `/actuator/health` - Status da aplicação
- **Sem autenticação**
- **Resposta esperada:**
```json
{
  "status": "UP"
}
```

---

## 🧪 Fluxo Completo de Teste

### **Cenário: Cliente fazendo um pedido**

1. **Login como cliente:**
```
POST /api/auth/login
{
  "email": "cliente@exemplo.com",
  "password": "Cliente@123"
}
```

2. **Ver cardápio:**
```
GET /api/menu-items
```

3. **Criar pedido:**
```
POST /api/orders
{
  "customerId": 2,
  "items": [
    { "menuItemId": 1, "quantity": 1 },
    { "menuItemId": 20, "quantity": 2 }
  ]
}
```

4. **Criar pagamento:**
```
POST /api/payments
{
  "orderId": 1,
  "method": "PIX",
  "amount": 89.80
}
```

5. **Aprovar pagamento (admin):**
```
PUT /api/payments/1/status?status=APPROVED
```

6. **Atualizar status do pedido (funcionário):**
```
PUT /api/orders/1/status?status=CONFIRMED
PUT /api/orders/1/status?status=IN_PREPARATION
PUT /api/orders/1/status?status=READY
PUT /api/orders/1/status?status=DELIVERED
```

7. **Verificar pontos de fidelidade:**
```
GET /api/loyalty/2
```

---

## 🎯 Dicas Importantes

### **1. Testar Autenticação**
Sempre faça login primeiro e guarde o token para usar nas outras requisições.

### **2. Verificar Roles (Permissões)**
- **ADMIN:** Acesso total
- **EMPLOYEE:** Pode gerenciar pedidos e pagamentos
- **CUSTOMER:** Pode criar pedidos e ver seu histórico

### **3. Testar Erros**
Teste também cenários de erro:
- Token inválido → 401 Unauthorized
- Sem token → 403 Forbidden
- ID inexistente → 404 Not Found
- Dados inválidos → 400 Bad Request

### **4. Usar Environments no Postman**
Crie um environment com:
- `baseUrl`: `http://localhost:8080`
- `token`: `{{token}}` (atualiza automaticamente após login)

---

## 📦 Collection Postman (Importar)

Você pode criar uma Collection no Postman com todas essas requisições e salvá-la para reutilizar!

**Passos:**
1. Abra Postman
2. Clique em **Import**
3. Cole o JSON abaixo ou crie manualmente

---

## 🆘 Troubleshooting

### **Erro 401 Unauthorized**
- Verifique se o token está correto
- Faça login novamente para obter novo token

### **Erro 403 Forbidden**
- Você não tem permissão para essa ação
- Faça login com usuário ADMIN

### **Erro 404 Not Found**
- Verifique se o ID existe no banco
- Use `/api/users` ou `/api/menu-items` para ver IDs disponíveis

### **Erro 500 Internal Server Error**
- Verifique os logs da aplicação
- Verifique se o PostgreSQL está rodando
- Confira se o banco foi inicializado com `init.sql`

---

## ✅ Checklist de Testes

- [ ] Login com admin funciona
- [ ] Login com cliente funciona
- [ ] Listar todos os usuários
- [ ] Listar cardápio completo
- [ ] Filtrar cardápio por categoria
- [ ] Criar novo pedido
- [ ] Atualizar status do pedido
- [ ] Criar pagamento
- [ ] Aprovar pagamento
- [ ] Ver pontos de fidelidade
- [ ] Health check retorna UP

---

**🎉 Pronto para testar! Boa sorte!** 🚀
