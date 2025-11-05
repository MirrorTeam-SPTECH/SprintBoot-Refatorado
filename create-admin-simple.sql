-- Script simplificado para criar usuário admin
-- Execute no pgAdmin

-- Deletar admin se já existir
DELETE FROM users WHERE email = 'admin@churras.com';

-- Criar novo admin
-- Senha: senha123
-- Hash BCrypt: $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

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

-- Verificar se foi criado
SELECT id, name, email, role, active FROM users WHERE email = 'admin@churras.com';
