-- Script para criar usuário admin com senha BCrypt
-- Senha: admin123
-- Hash BCrypt gerado para "admin123"

-- Deletar usuário admin existente (se houver)
DELETE FROM users WHERE email = 'admin@churras.com';

-- Inserir novo usuário admin com senha BCrypt
INSERT INTO users (name, email, password, phone, role, active, created_at, updated_at) 
VALUES (
    'Administrador Portal Churras',
    'admin@churras.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',  -- Senha: admin123
    '11999999999',
    'ADMIN',
    true,
    NOW(),
    NOW()
);

-- Verificar se foi criado
SELECT id, name, email, role FROM users WHERE email = 'admin@churras.com';
