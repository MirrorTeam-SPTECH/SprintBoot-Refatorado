# 🔌 Guia Rápido de Integração Frontend
## Portal Churras - Conectar React/Vue/Angular ao Backend

---

## 📦 Instalação de Dependências

### Para WebSocket (Recomendado)
```bash
npm install sockjs-client @stomp/stompjs
```

### Para HTTP Requests
```bash
# Axios (recomendado)
npm install axios

# Ou usar fetch nativo do browser
```

---

## ⚙️ Configuração da API

### 1. Criar arquivo de configuração (`src/config/api.js`)
```javascript
// Desenvolvimento local
const API_BASE_URL = 'http://localhost:8080';

// Produção AWS (via Nginx load balancer)
// const API_BASE_URL = 'http://10.0.2.11';

const WS_BASE_URL = API_BASE_URL.replace('http', 'ws');

export { API_BASE_URL, WS_BASE_URL };
```

---

## 🔐 Autenticação (Login)

### Componente de Login
```javascript
import { API_BASE_URL } from './config/api';

async function login(email, password) {
  try {
    const response = await fetch(`${API_BASE_URL}/api/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include', // Para cookies
      body: JSON.stringify({ email, password })
    });

    if (!response.ok) {
      throw new Error('Login falhou');
    }

    const data = await response.json();
    
    // Salvar token no localStorage
    localStorage.setItem('token', data.token);
    localStorage.setItem('user', JSON.stringify(data.user));
    
    return data;
  } catch (error) {
    console.error('Erro no login:', error);
    throw error;
  }
}

// Uso:
// login('cliente@email.com', 'Cliente@123')
//   .then(data => console.log('Logado:', data))
//   .catch(err => console.error('Erro:', err));
```

### Registro de Cliente
```javascript
async function register(name, email, password, phone) {
  const response = await fetch(`${API_BASE_URL}/api/users/register`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      name,
      email,
      password,
      phone,
      role: 'CUSTOMER' // Sempre CUSTOMER no registro público
    })
  });

  return response.json();
}
```

---

## 🍔 Buscar Cardápio (Público)

### Listar Todos os Itens
```javascript
async function getMenuItems() {
  const response = await fetch(`${API_BASE_URL}/api/menu-items`);
  const items = await response.json();
  return items; // Array com 27 itens
}

// Uso:
// getMenuItems().then(items => {
//   console.log('Total de itens:', items.length);
//   items.forEach(item => {
//     console.log(`${item.name} - R$ ${item.price}`);
//   });
// });
```

### Buscar por Categoria
```javascript
async function getMenuByCategory(category) {
  const response = await fetch(
    `${API_BASE_URL}/api/menu-items/category/${category}`
  );
  return response.json();
}

// Categorias disponíveis:
// - COMBOS
// - HAMBURGUERES
// - ESPETINHOS
// - PORCOES
// - BEBIDAS
// - ADICIONAIS
```

---

## 🛒 Criar Pedido (Requer Autenticação)

### Função Helper para Headers Autenticados
```javascript
function getAuthHeaders() {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    'Authorization': `Bearer ${token}`
  };
}
```

### Criar Pedido
```javascript
async function createOrder(items) {
  const response = await fetch(`${API_BASE_URL}/api/orders`, {
    method: 'POST',
    headers: getAuthHeaders(),
    credentials: 'include',
    body: JSON.stringify({
      items: items.map(item => ({
        menuItemId: item.id,
        quantity: item.quantity,
        notes: item.notes || ''
      })),
      notes: 'Pedido online'
    })
  });

  return response.json();
}

// Exemplo de uso:
// const cart = [
//   { id: 1, quantity: 2, notes: 'Sem cebola' },
//   { id: 5, quantity: 1, notes: 'Bem passado' }
// ];
// 
// createOrder(cart).then(order => {
//   console.log('Pedido criado:', order.id);
//   console.log('Total:', order.total);
// });
```

---

## 📦 Buscar Pedidos do Usuário

```javascript
async function getMyOrders() {
  const response = await fetch(`${API_BASE_URL}/api/orders`, {
    headers: getAuthHeaders(),
    credentials: 'include'
  });

  return response.json();
}

// Retorna array de pedidos:
// [
//   {
//     id: 1,
//     status: 'PENDING',
//     total: 89.90,
//     items: [...],
//     createdAt: '2025-10-30T19:30:00'
//   }
// ]
```

---

## 💳 Criar Pagamento (Mercado Pago)

```javascript
async function createPayment(orderId, paymentMethod) {
  const response = await fetch(`${API_BASE_URL}/api/payments`, {
    method: 'POST',
    headers: getAuthHeaders(),
    credentials: 'include',
    body: JSON.stringify({
      orderId: orderId,
      paymentMethod: paymentMethod // 'PIX', 'CREDIT_CARD', 'DEBIT_CARD'
    })
  });

  const payment = await response.json();
  
  // Se for PIX, payment.qrCode terá o código PIX
  // Se for cartão, payment.paymentUrl terá a URL do Mercado Pago
  
  return payment;
}

// Exemplo:
// createPayment(123, 'PIX').then(payment => {
//   console.log('QR Code PIX:', payment.qrCode);
//   // Exibir QR Code para o cliente
// });
```

---

## 🎁 Programa de Fidelidade

### Buscar Pontos do Usuário
```javascript
async function getLoyaltyProgram() {
  const response = await fetch(`${API_BASE_URL}/api/loyalty`, {
    headers: getAuthHeaders(),
    credentials: 'include'
  });

  return response.json();
}

// Retorna:
// {
//   totalPoints: 1500,
//   availablePoints: 1000,
//   tier: 'SILVER', // BRONZE, SILVER, GOLD, DIAMOND
//   totalSpent: 500.00
// }
```

### Resgatar Pontos
```javascript
async function redeemPoints(points, description) {
  const response = await fetch(`${API_BASE_URL}/api/loyalty/redeem`, {
    method: 'POST',
    headers: getAuthHeaders(),
    credentials: 'include',
    body: JSON.stringify({
      pointsUsed: points,
      description: description
    })
  });

  return response.json();
}

// Exemplo:
// redeemPoints(500, 'Desconto de R$ 25').then(result => {
//   console.log('Pontos resgatados com sucesso!');
// });
```

---

## 🔴 WebSocket (Notificações em Tempo Real)

### Configurar Conexão WebSocket
```javascript
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { API_BASE_URL } from './config/api';

let stompClient = null;

function connectWebSocket() {
  const socket = new SockJS(`${API_BASE_URL}/ws`);
  stompClient = Stomp.over(socket);

  stompClient.connect({}, (frame) => {
    console.log('WebSocket conectado:', frame);

    // Inscrever em novos pedidos (para admin/funcionários)
    stompClient.subscribe('/topic/orders', (message) => {
      const order = JSON.parse(message.body);
      console.log('Novo pedido recebido:', order);
      // Atualizar UI com novo pedido
      showNotification('Novo pedido #' + order.id);
    });

    // Inscrever em atualizações de menu
    stompClient.subscribe('/topic/menu-updates', (message) => {
      const menuUpdate = JSON.parse(message.body);
      console.log('Menu atualizado:', menuUpdate);
      // Recarregar lista de itens
      refreshMenuItems();
    });

    // Notificações pessoais do usuário
    const userId = JSON.parse(localStorage.getItem('user')).id;
    stompClient.subscribe(`/user/${userId}/queue/notifications`, (message) => {
      const notification = JSON.parse(message.body);
      console.log('Notificação pessoal:', notification);
      showNotification(notification.message);
    });
  });

  stompClient.onStompError = (error) => {
    console.error('Erro WebSocket:', error);
  };
}

function disconnectWebSocket() {
  if (stompClient !== null) {
    stompClient.disconnect();
    console.log('WebSocket desconectado');
  }
}

// Chamar ao fazer login
// connectWebSocket();

// Chamar ao fazer logout
// disconnectWebSocket();
```

---

## 🛡️ Interceptor de Requisições (Axios)

### Configurar Axios com Token Automático
```javascript
import axios from 'axios';
import { API_BASE_URL } from './config/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true
});

// Interceptor para adicionar token automaticamente
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor para tratar erros 401 (não autorizado)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      // Token expirado - redirecionar para login
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default api;
```

### Usar Axios Configurado
```javascript
import api from './config/api-interceptor';

// Login
async function login(email, password) {
  const response = await api.post('/api/auth/login', { email, password });
  localStorage.setItem('token', response.data.token);
  return response.data;
}

// Buscar cardápio
async function getMenuItems() {
  const response = await api.get('/api/menu-items');
  return response.data;
}

// Criar pedido (com token automático)
async function createOrder(items) {
  const response = await api.post('/api/orders', { items });
  return response.data;
}
```

---

## 🎨 Componente React Completo (Exemplo)

```jsx
import React, { useState, useEffect } from 'react';
import api from './config/api-interceptor';

function MenuList() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [cart, setCart] = useState([]);

  useEffect(() => {
    loadMenuItems();
  }, []);

  async function loadMenuItems() {
    try {
      const data = await api.get('/api/menu-items');
      setItems(data.data);
    } catch (error) {
      console.error('Erro ao carregar cardápio:', error);
    } finally {
      setLoading(false);
    }
  }

  function addToCart(item) {
    const existingItem = cart.find(i => i.id === item.id);
    if (existingItem) {
      setCart(cart.map(i => 
        i.id === item.id 
          ? { ...i, quantity: i.quantity + 1 } 
          : i
      ));
    } else {
      setCart([...cart, { ...item, quantity: 1 }]);
    }
  }

  async function checkout() {
    try {
      const order = await api.post('/api/orders', {
        items: cart.map(item => ({
          menuItemId: item.id,
          quantity: item.quantity,
          notes: ''
        }))
      });
      alert(`Pedido #${order.data.id} criado! Total: R$ ${order.data.total}`);
      setCart([]);
    } catch (error) {
      alert('Erro ao criar pedido: ' + error.message);
    }
  }

  if (loading) return <div>Carregando...</div>;

  return (
    <div>
      <h1>Cardápio</h1>
      <div className="menu-items">
        {items.map(item => (
          <div key={item.id} className="menu-item">
            <h3>{item.name}</h3>
            <p>{item.description}</p>
            <p>R$ {item.price.toFixed(2)}</p>
            <button onClick={() => addToCart(item)}>
              Adicionar ao Carrinho
            </button>
          </div>
        ))}
      </div>

      <div className="cart">
        <h2>Carrinho ({cart.length} itens)</h2>
        {cart.map(item => (
          <div key={item.id}>
            {item.name} x{item.quantity} = R$ {(item.price * item.quantity).toFixed(2)}
          </div>
        ))}
        {cart.length > 0 && (
          <button onClick={checkout}>
            Finalizar Pedido - R$ {cart.reduce((sum, item) => sum + (item.price * item.quantity), 0).toFixed(2)}
          </button>
        )}
      </div>
    </div>
  );
}

export default MenuList;
```

---

## 🚨 Tratamento de Erros

```javascript
async function makeRequest() {
  try {
    const response = await api.get('/api/orders');
    return response.data;
  } catch (error) {
    if (error.response) {
      // Servidor retornou erro (4xx, 5xx)
      switch (error.response.status) {
        case 400:
          console.error('Requisição inválida:', error.response.data);
          break;
        case 401:
          console.error('Não autorizado - fazer login novamente');
          // Redirecionar para /login
          break;
        case 403:
          console.error('Acesso negado - permissão insuficiente');
          break;
        case 404:
          console.error('Recurso não encontrado');
          break;
        case 500:
          console.error('Erro no servidor');
          break;
        default:
          console.error('Erro:', error.response.data);
      }
    } else if (error.request) {
      // Requisição foi feita mas sem resposta
      console.error('Servidor não respondeu - verificar conexão');
    } else {
      // Erro ao configurar requisição
      console.error('Erro ao fazer requisição:', error.message);
    }
    throw error;
  }
}
```

---

## ✅ Checklist de Integração

### Antes de Começar
- [ ] Backend rodando em `http://localhost:8080` (dev) ou `http://10.0.2.11` (prod)
- [ ] Dependências instaladas (`sockjs-client`, `@stomp/stompjs`, `axios`)
- [ ] Arquivo de configuração da API criado

### Implementação Básica
- [ ] Sistema de login funcionando
- [ ] Token sendo salvo no localStorage
- [ ] Headers de autenticação sendo enviados
- [ ] Listagem de cardápio funcionando
- [ ] Criar pedido funcionando

### Implementação Avançada
- [ ] WebSocket conectado e recebendo notificações
- [ ] Interceptor de erros 401 configurado
- [ ] Carrinho de compras implementado
- [ ] Integração com Mercado Pago (pagamentos)
- [ ] Sistema de fidelidade funcionando

### Testes
- [ ] Testar login com credenciais corretas
- [ ] Testar login com credenciais incorretas
- [ ] Testar criação de pedido
- [ ] Testar acesso a rota protegida sem token
- [ ] Testar CORS (requisições do frontend AWS funcionando)
- [ ] Testar WebSocket (recebendo notificações)

---

## 📞 Endpoints Úteis para Teste

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

### Listar Menu (sem autenticação)
```bash
curl http://localhost:8080/api/menu-items
```

### Login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"cliente@email.com","password":"Cliente@123"}'
```

---

## 🎯 Credenciais de Teste

```
Admin:
  Email: admin@portalchurras.com
  Senha: Admin@123

Funcionário:
  Email: funcionario@portalchurras.com
  Senha: Func@123

Cliente:
  Email: cliente@email.com
  Senha: Cliente@123
```

---

**Pronto! Frontend configurado para integração completa com o backend Portal Churras.**

**Próximo passo:** Implementar os componentes usando os exemplos acima. ✅
