-- Script para transformar um usuário existente em ADMIN
-- Execute no pgAdmin

-- 1. Verificar se o usuário existe
SELECT id, name, email, role, active 
FROM users 
WHERE email = 'admin@churras.com';

-- 2. Se o usuário não existir, você precisa cadastrar pela tela do site primeiro!
-- Se existir, execute o comando abaixo para mudar o role para ADMIN:

UPDATE users 
SET role = 'ADMIN', 
    updated_at = NOW()
WHERE email = 'admin@churras.com';

-- 3. Verificar se foi atualizado
SELECT id, name, email, role, active 
FROM users 
WHERE email = 'admin@churras.com';
