-- Script para resetar usuários e criar novos para teste
-- Execute no pgAdmin antes de iniciar os testes

-- 1. DELETAR TODOS OS USUÁRIOS EXISTENTES
-- Primeiro, deletar registros relacionados (se houver foreign keys)
DELETE FROM user_roles;
DELETE FROM users;

-- 2. RESETAR A SEQUÊNCIA DE IDs (opcional)
ALTER SEQUENCE users_id_seq RESTART WITH 1;

-- 3. CRIAR USUÁRIOS DE TESTE
-- Senha padrão para todos: "senha123"
-- Hash BCrypt da senha "senha123": $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

-- Admin 1
INSERT INTO users (name, email, password, phone, role, active, created_at, updated_at) 
VALUES (
    'Administrador',
    'admin@churras.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '(11) 99999-0001',
    'ADMIN',
    true,
    NOW(),
    NOW()
);

-- Admin 2
INSERT INTO users (name, email, password, phone, role, active, created_at, updated_at) 
VALUES (
    'João Silva',
    'joao.admin@churras.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '(11) 99999-0002',
    'ADMIN',
    true,
    NOW(),
    NOW()
);

-- Funcionário 1
INSERT INTO users (name, email, password, phone, role, active, created_at, updated_at) 
VALUES (
    'Maria Santos',
    'maria.func@churras.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '(11) 99999-0003',
    'EMPLOYEE',
    true,
    NOW(),
    NOW()
);

-- Funcionário 2
INSERT INTO users (name, email, password, phone, role, active, created_at, updated_at) 
VALUES (
    'Pedro Oliveira',
    'pedro.func@churras.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '(11) 99999-0004',
    'EMPLOYEE',
    true,
    NOW(),
    NOW()
);

-- Cliente 1
INSERT INTO users (name, email, password, phone, role, active, created_at, updated_at) 
VALUES (
    'Ana Costa',
    'ana@email.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '(11) 98888-0001',
    'CUSTOMER',
    true,
    NOW(),
    NOW()
);

-- Cliente 2
INSERT INTO users (name, email, password, phone, role, active, created_at, updated_at) 
VALUES (
    'Carlos Pereira',
    'carlos@email.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '(11) 98888-0002',
    'CUSTOMER',
    true,
    NOW(),
    NOW()
);

-- Cliente 3
INSERT INTO users (name, email, password, phone, role, active, created_at, updated_at) 
VALUES (
    'Beatriz Lima',
    'beatriz@email.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    '(11) 98888-0003',
    'CUSTOMER',
    true,
    NOW(),
    NOW()
);

-- 4. VERIFICAR OS USUÁRIOS CRIADOS
SELECT 
    id,
    name,
    email,
    phone,
    role,
    active,
    created_at
FROM users
ORDER BY role DESC, id;

-- 5. INFORMAÇÕES PARA LOGIN
-- ====================================
-- ADMINISTRADORES:
-- Email: admin@churras.com | Senha: senha123
-- Email: joao.admin@churras.com | Senha: senha123
--
-- FUNCIONÁRIOS:
-- Email: maria.func@churras.com | Senha: senha123
-- Email: pedro.func@churras.com | Senha: senha123
--
-- CLIENTES:
-- Email: ana@email.com | Senha: senha123
-- Email: carlos@email.com | Senha: senha123
-- Email: beatriz@email.com | Senha: senha123
-- ====================================
