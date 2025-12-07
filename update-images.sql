-- Script para atualizar as URLs das imagens dos itens do cardápio
-- Execute este script no PostgreSQL após o init.sql

-- =========================================
-- COMBOS
-- =========================================
UPDATE menu_items SET image_url = '/img/combo-1.png' WHERE name = 'Combo Família';
UPDATE menu_items SET image_url = '/img/combo-2.png' WHERE name = 'Combo Casal';
UPDATE menu_items SET image_url = '/img/combo-3.png' WHERE name = 'Combo Individual';

-- =========================================
-- HAMBÚRGUERES
-- =========================================
UPDATE menu_items SET image_url = '/img/x-burguer.png' WHERE name = 'X-Burger';
UPDATE menu_items SET image_url = '/img/x-bacon.png' WHERE name = 'X-Bacon';
UPDATE menu_items SET image_url = '/img/x-eggbacon.png' WHERE name = 'X-Egg';
UPDATE menu_items SET image_url = '/img/x-cheddarbacon.png' WHERE name = 'X-Tudo';
UPDATE menu_items SET image_url = '/img/x-salada.png' WHERE name = 'Veggie Burger';

-- =========================================
-- ESPETINHOS
-- =========================================
UPDATE menu_items SET image_url = '/img/espeto-carne.png' WHERE name = 'Espetinho de Carne';
UPDATE menu_items SET image_url = '/img/espeto-frango.png' WHERE name = 'Espetinho de Frango';
UPDATE menu_items SET image_url = '/img/espeto-cafta.png' WHERE name = 'Espetinho de Kafta';
UPDATE menu_items SET image_url = '/img/espeto-linguica.png' WHERE name = 'Espetinho de Linguiça';
UPDATE menu_items SET image_url = '/img/espetinhos.png' WHERE name = 'Espetinho Vegetariano';

-- =========================================
-- PORÇÕES
-- =========================================
UPDATE menu_items SET image_url = '/img/porcao-fritas.png' WHERE name = 'Batata Frita P';
UPDATE menu_items SET image_url = '/img/porcao-fritas.png' WHERE name = 'Batata Frita M';
UPDATE menu_items SET image_url = '/img/porcao-fritas.png' WHERE name = 'Batata Frita G';
UPDATE menu_items SET image_url = '/img/batata-cheddar.png' WHERE name = 'Onion Rings';
UPDATE menu_items SET image_url = '/img/porcao-fritas.png' WHERE name = 'Nuggets';

-- =========================================
-- BEBIDAS
-- =========================================
UPDATE menu_items SET image_url = '/img/refri_latas.png' WHERE name = 'Refrigerante Lata';
UPDATE menu_items SET image_url = '/img/coca-cola.png' WHERE name = 'Refrigerante 600ml';
UPDATE menu_items SET image_url = '/img/coca-cola.png' WHERE name = 'Refrigerante 2L';
UPDATE menu_items SET image_url = '/img/suco.png' WHERE name = 'Suco Natural';
UPDATE menu_items SET image_url = '/img/suco.png' WHERE name = 'Água Mineral';

-- =========================================
-- ADICIONAIS
-- =========================================
UPDATE menu_items SET image_url = '/img/adicional-bacon.png' WHERE name = 'Bacon Extra';
UPDATE menu_items SET image_url = '/img/adicional-queijo.png' WHERE name = 'Queijo Extra';
UPDATE menu_items SET image_url = '/img/adicional-hamburguer.png' WHERE name = 'Ovo Extra';
UPDATE menu_items SET image_url = '/img/adicional-cheddar.png' WHERE name = 'Molho Especial';

-- =========================================
-- VERIFICAR ATUALIZAÇÃO
-- =========================================
SELECT id, name, category, image_url FROM menu_items ORDER BY category, name;
