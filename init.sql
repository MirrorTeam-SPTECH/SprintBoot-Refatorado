-- init.sql - Script de inicialização do banco de dados PostgreSQL
-- IMPORTANTE: Este script deve ser executado APÓS o Hibernate criar as tabelas
-- O Hibernate cria as tabelas automaticamente com spring.jpa.hibernate.ddl-auto=update

-- Configurações
SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SET check_function_bodies = false;
SET client_min_messages = warning;
SET row_security = off;

-- Extensões úteis
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =========================================
-- AGUARDAR HIBERNATE CRIAR AS TABELAS
-- =========================================
-- O Hibernate criará automaticamente:
-- - users
-- - orders
-- - order_items
-- - menu_items
-- - payments
-- - loyalty_programs
-- - loyalty_transactions
-- =========================================

-- =========================================
-- ÍNDICES (Melhoram performance das queries)
-- =========================================
-- Executar SOMENTE após as tabelas existirem

-- Índices para Orders
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_status_created ON orders(status, created_at);

-- Índices para Payments
CREATE INDEX IF NOT EXISTS idx_payments_order_id ON payments(order_id);
CREATE INDEX IF NOT EXISTS idx_payments_status ON payments(status);
CREATE INDEX IF NOT EXISTS idx_payments_created_at ON payments(created_at);

-- Índices para Menu Items
CREATE INDEX IF NOT EXISTS idx_menu_items_category ON menu_items(category);
CREATE INDEX IF NOT EXISTS idx_menu_items_active ON menu_items(active);
CREATE INDEX IF NOT EXISTS idx_menu_items_active_category ON menu_items(active, category);

-- Índices para Users
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(active);

-- Índices para Loyalty Programs
CREATE INDEX IF NOT EXISTS idx_loyalty_user_id ON loyalty_programs(user_id);
CREATE INDEX IF NOT EXISTS idx_loyalty_tier ON loyalty_programs(tier);

-- Índices para Loyalty Transactions
CREATE INDEX IF NOT EXISTS idx_loyalty_transactions_program ON loyalty_transactions(loyalty_program_id);
CREATE INDEX IF NOT EXISTS idx_loyalty_transactions_created ON loyalty_transactions(created_at);

-- Índices para Order Items
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_menu_item_id ON order_items(menu_item_id);

-- =========================================
-- DADOS INICIAIS
-- =========================================

-- IMPORTANTE: As senhas abaixo são hashes BCrypt
-- Use o Spring Boot para gerar os hashes corretos
-- Estas são apenas placeholders e devem ser substituídas

-- Usuário Admin (email: admin@portalchurras.com, senha: Admin@123)
-- Hash gerado com BCrypt strength 10
INSERT INTO users (name, email, password, phone, role, active, created_at, updated_at) 
VALUES 
('Administrador', 'admin@portalchurras.com', '$2a$10$xQjKHvD9kcZhVHKqQqH.WeO7bC3Y8H9H9GHYF.Qhv1VqGf2h.EHmO', '11999999999', 'ADMIN', true, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- Usuário Funcionário (email: funcionario@portalchurras.com, senha: Func@123)
INSERT INTO users (name, email, password, phone, role, active, created_at, updated_at) 
VALUES 
('Funcionário Teste', 'funcionario@portalchurras.com', '$2a$10$xQjKHvD9kcZhVHKqQqH.WeO7bC3Y8H9H9GHYF.Qhv1VqGf2h.EHmO', '11988888888', 'EMPLOYEE', true, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- Cliente Teste (email: cliente@email.com, senha: Cliente@123)
INSERT INTO users (name, email, password, phone, role, active, created_at, updated_at) 
VALUES 
('Cliente Teste', 'cliente@email.com', '$2a$10$xQjKHvD9kcZhVHKqQqH.WeO7bC3Y8H9H9GHYF.Qhv1VqGf2h.EHmO', '11977777777', 'CUSTOMER', true, NOW(), NOW())
ON CONFLICT (email) DO NOTHING;

-- =========================================
-- ITENS DO CARDÁPIO
-- =========================================
INSERT INTO menu_items (name, description, price, category, preparation_time, active, created_at, updated_at) VALUES
-- Combos
('Combo Família', 'X-Burger + X-Bacon + Batata Grande + Refrigerante 2L', 79.90, 'COMBOS', '30 min', true, NOW(), NOW()),
('Combo Casal', '2 X-Burgers + Batata Média + 2 Refrigerantes', 54.90, 'COMBOS', '25 min', true, NOW(), NOW()),
('Combo Individual', 'X-Burger + Batata Pequena + Refrigerante', 29.90, 'COMBOS', '15 min', true, NOW(), NOW()),

-- Hambúrgueres
('X-Burger', 'Hambúrguer, queijo, alface, tomate, molho especial', 18.90, 'HAMBURGUERES', '15 min', true, NOW(), NOW()),
('X-Bacon', 'X-Burger + bacon crocante', 22.90, 'HAMBURGUERES', '15 min', true, NOW(), NOW()),
('X-Egg', 'X-Burger + ovo', 20.90, 'HAMBURGUERES', '15 min', true, NOW(), NOW()),
('X-Tudo', 'Hambúrguer, queijo, bacon, ovo, presunto, alface, tomate', 28.90, 'HAMBURGUERES', '20 min', true, NOW(), NOW()),
('Veggie Burger', 'Hambúrguer vegetariano com queijo e salada', 19.90, 'HAMBURGUERES', '15 min', true, NOW(), NOW()),

-- Espetinhos
('Espetinho de Carne', 'Carne bovina temperada', 8.00, 'ESPETINHOS', '10 min', true, NOW(), NOW()),
('Espetinho de Frango', 'Frango marinado especial', 7.00, 'ESPETINHOS', '10 min', true, NOW(), NOW()),
('Espetinho de Kafta', 'Kafta artesanal temperada', 9.00, 'ESPETINHOS', '12 min', true, NOW(), NOW()),
('Espetinho de Linguiça', 'Linguiça artesanal', 8.00, 'ESPETINHOS', '10 min', true, NOW(), NOW()),
('Espetinho Vegetariano', 'Mix de legumes grelhados', 6.00, 'ESPETINHOS', '8 min', true, NOW(), NOW()),

-- Porções
('Batata Frita P', 'Porção pequena de batata frita', 10.00, 'PORCOES', '10 min', true, NOW(), NOW()),
('Batata Frita M', 'Porção média de batata frita', 15.00, 'PORCOES', '10 min', true, NOW(), NOW()),
('Batata Frita G', 'Porção grande de batata frita', 20.00, 'PORCOES', '12 min', true, NOW(), NOW()),
('Onion Rings', 'Anéis de cebola empanados', 18.00, 'PORCOES', '12 min', true, NOW(), NOW()),
('Nuggets', '10 unidades de nuggets de frango', 16.00, 'PORCOES', '10 min', true, NOW(), NOW()),

-- Bebidas
('Refrigerante Lata', 'Coca-Cola, Guaraná ou Sprite', 5.00, 'BEBIDAS', '1 min', true, NOW(), NOW()),
('Refrigerante 600ml', 'Coca-Cola, Guaraná ou Sprite', 8.00, 'BEBIDAS', '1 min', true, NOW(), NOW()),
('Refrigerante 2L', 'Coca-Cola, Guaraná ou Sprite', 12.00, 'BEBIDAS', '1 min', true, NOW(), NOW()),
('Suco Natural', 'Laranja, Limão ou Maracujá', 8.00, 'BEBIDAS', '5 min', true, NOW(), NOW()),
('Água Mineral', 'Garrafa 500ml', 3.00, 'BEBIDAS', '1 min', true, NOW(), NOW()),

-- Adicionais
('Bacon Extra', 'Porção extra de bacon', 5.00, 'ADICIONAIS', '2 min', true, NOW(), NOW()),
('Queijo Extra', 'Fatia extra de queijo', 3.00, 'ADICIONAIS', '1 min', true, NOW(), NOW()),
('Ovo Extra', 'Ovo frito adicional', 3.00, 'ADICIONAIS', '3 min', true, NOW(), NOW()),
('Molho Especial', 'Porção extra do molho da casa', 2.00, 'ADICIONAIS', '1 min', true, NOW(), NOW())
ON CONFLICT DO NOTHING;

-- =========================================
-- FUNÇÕES E TRIGGERS (Automatização)
-- =========================================

-- Função para calcular pontos de fidelidade automaticamente
CREATE OR REPLACE FUNCTION calculate_loyalty_points()
RETURNS TRIGGER AS $$
BEGIN
    -- Se o pedido foi aprovado e tem um cliente registrado
    IF NEW.status = 'DELIVERED' AND OLD.status != 'DELIVERED' AND NEW.customer_id IS NOT NULL THEN
        -- Adicionar pontos (1 real = 10 pontos base)
        UPDATE loyalty_programs 
        SET total_points = total_points + (NEW.total * 10)::INTEGER,
            available_points = available_points + (NEW.total * 10)::INTEGER,
            total_spent = total_spent + NEW.total,
            total_orders = total_orders + 1,
            last_activity_at = NOW()
        WHERE user_id = NEW.customer_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Criar trigger para calcular pontos
DROP TRIGGER IF EXISTS trigger_calculate_loyalty ON orders;
CREATE TRIGGER trigger_calculate_loyalty
    AFTER UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION calculate_loyalty_points();

-- Função para atualizar tier automaticamente
CREATE OR REPLACE FUNCTION update_loyalty_tier()
RETURNS TRIGGER AS $$
BEGIN
    -- Atualizar tier baseado nos pontos totais
    IF NEW.total_points >= 5000 THEN
        NEW.tier = 'DIAMOND';
    ELSIF NEW.total_points >= 2500 THEN
        NEW.tier = 'GOLD';
    ELSIF NEW.total_points >= 1000 THEN
        NEW.tier = 'SILVER';
    ELSE
        NEW.tier = 'BRONZE';
    END IF;
    
    -- Se houve mudança de tier, atualizar data
    IF OLD.tier != NEW.tier THEN
        NEW.tier_upgrade_at = NOW();
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Criar trigger para atualizar tier
DROP TRIGGER IF EXISTS trigger_update_tier ON loyalty_programs;
CREATE TRIGGER trigger_update_tier
    BEFORE UPDATE ON loyalty_programs
    FOR EACH ROW
    EXECUTE FUNCTION update_loyalty_tier();

-- View para dashboard de vendas
CREATE OR REPLACE VIEW v_daily_sales AS
SELECT 
    DATE(created_at) as sale_date,
    COUNT(*) as total_orders,
    SUM(CASE WHEN status != 'CANCELLED' THEN 1 ELSE 0 END) as completed_orders,
    SUM(CASE WHEN status = 'CANCELLED' THEN 1 ELSE 0 END) as cancelled_orders,
    SUM(CASE WHEN status != 'CANCELLED' THEN total ELSE 0 END) as total_revenue,
    AVG(CASE WHEN status != 'CANCELLED' THEN total ELSE NULL END) as avg_ticket
FROM orders
GROUP BY DATE(created_at)
ORDER BY sale_date DESC;

-- View para produtos mais vendidos
CREATE OR REPLACE VIEW v_top_products AS
SELECT 
    mi.name as product_name,
    mi.category,
    COUNT(oi.id) as times_ordered,
    SUM(oi.quantity) as total_quantity,
    SUM(oi.total_price) as total_revenue
FROM order_items oi
JOIN menu_items mi ON oi.menu_item_id = mi.id
JOIN orders o ON oi.order_id = o.id
WHERE o.status != 'CANCELLED'
GROUP BY mi.id, mi.name, mi.category
ORDER BY total_quantity DESC;

-- View para análise de clientes
CREATE OR REPLACE VIEW v_customer_analytics AS
SELECT 
    u.id as user_id,
    u.name,
    u.email,
    COUNT(o.id) as total_orders,
    SUM(CASE WHEN o.status != 'CANCELLED' THEN o.total ELSE 0 END) as total_spent,
    AVG(CASE WHEN o.status != 'CANCELLED' THEN o.total ELSE NULL END) as avg_order_value,
    MAX(o.created_at) as last_order_date,
    lp.tier as loyalty_tier,
    lp.available_points as loyalty_points
FROM users u
LEFT JOIN orders o ON u.id = o.customer_id
LEFT JOIN loyalty_programs lp ON u.id = lp.user_id
WHERE u.role = 'CUSTOMER'
GROUP BY u.id, u.name, u.email, lp.tier, lp.available_points
ORDER BY total_spent DESC;

-- =========================================
-- ÍNDICES ADICIONAIS PARA VIEWS
-- =========================================
-- Nota: Índices básicos já criados na seção anterior
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_menu_item_id ON order_items(menu_item_id);

-- =========================================
-- SCRIPT CONCLUÍDO
-- =========================================
-- Ordem de execução:
-- 1. Extensões (pgcrypto) ✓
-- 2. Aguardar Hibernate criar tabelas
-- 3. Índices de performance ✓
-- 4. Dados iniciais (usuários + menu) ✓
-- 5. Funções e triggers de automação ✓
-- 6. Views analíticas ✓
--
-- NOTA: Configurações de performance do PostgreSQL devem ser
-- ajustadas no arquivo postgresql.conf ou via AWS RDS console
-- (ALTER SYSTEM requer privilégios de superusuário)
-- =========================================