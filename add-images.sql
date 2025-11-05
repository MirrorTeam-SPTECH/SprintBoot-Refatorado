-- Script para adicionar URLs de imagens aos produtos do cardápio
-- Execute este script no pgAdmin ou via psql

-- HAMBURGUERES
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=300&h=300&fit=crop' WHERE name = 'Veggie Burger';
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1553979459-d2229ba7433b?w=300&h=300&fit=crop' WHERE name = 'X-Bacon';
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1572802419224-296b0aeee0d9?w=300&h=300&fit=crop' WHERE name = 'X-Burger';
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1565299507177-b0ac66763828?w=300&h=300&fit=crop' WHERE name = 'X-Egg';
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1550547660-d9450f859349?w=300&h=300&fit=crop' WHERE name = 'X-Tudo';

-- ESPETINHOS
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1529193591184-b1d58069ecdd?w=300&h=300&fit=crop' WHERE name = 'Espetinho de Carne';
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1598103442097-8b74394b95c6?w=300&h=300&fit=crop' WHERE name = 'Espetinho de Frango';
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1633237308525-cd587cf71926?w=300&h=300&fit=crop' WHERE name = 'Espetinho de Linguiça';
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1565557623262-b51c2513a641?w=300&h=300&fit=crop' WHERE name = 'Espetinho de Queijo';
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1603073203605-b8c5b9f87f75?w=300&h=300&fit=crop' WHERE name = 'Espetinho Misto';

-- BEBIDAS
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1554866585-cd94860890b7?w=300&h=300&fit=crop' WHERE name = 'Coca-Cola';
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1437418747212-8d9709afab22?w=300&h=300&fit=crop' WHERE name = 'Guaraná';
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1560512823-829485b8bf24?w=300&h=300&fit=crop' WHERE name = 'Suco Natural';
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=300&h=300&fit=crop' WHERE name = 'Cerveja';
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=300&h=300&fit=crop' WHERE name = 'Água Mineral';

-- PORÇÕES
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1598679253544-2c97992403ea?w=300&h=300&fit=crop' WHERE name = 'Batata Frita';
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1626913392212-e5c0f1e5f758?w=300&h=300&fit=crop' WHERE name = 'Onion Rings';
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1619881429941-009d1c29b5e6?w=300&h=300&fit=crop' WHERE name = 'Mandioca Frita';
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1541592106381-b31e9677c0e5?w=300&h=300&fit=crop' WHERE name = 'Polenta Frita';
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1599487488170-d11ec9c172f0?w=300&h=300&fit=crop' WHERE name = 'Mix de Petiscos';

-- ADICIONAIS
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1587236119757-05b29d0d1230?w=300&h=300&fit=crop' WHERE name = 'Queijo Extra';
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1528607929212-2636ec44253e?w=300&h=300&fit=crop' WHERE name = 'Bacon Extra';
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1482049016688-2d3e1b311543?w=300&h=300&fit=crop' WHERE name = 'Ovo Extra';
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1472476443507-c7a5948772fc?w=300&h=300&fit=crop' WHERE name = 'Molho Especial';

-- COMBOS
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1561758033-d89a9ad46330?w=300&h=300&fit=crop' WHERE name = 'Combo Individual';
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1594212699903-ec8a3eca50f5?w=300&h=300&fit=crop' WHERE name = 'Combo Casal';
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=300&h=300&fit=crop' WHERE name = 'Combo Família';

-- CASO ALGUM PRODUTO TENHA NOME DIFERENTE (com acentos ou variações)
-- Atualizar todos os produtos que ainda não têm imagem com uma imagem padrão
UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1555939594-58d7cb561ad1?w=400' 
WHERE image_url IS NULL AND category = 'HAMBURGUERES';

UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1529193591184-b1d58069ecdd?w=400' 
WHERE image_url IS NULL AND category = 'ESPETINHOS';

UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1546173159-315724a31696?w=400' 
WHERE image_url IS NULL AND category = 'BEBIDAS';

UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1598679253544-2c97992403ea?w=400' 
WHERE image_url IS NULL AND category = 'PORCOES';

UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1587236119757-05b29d0d1230?w=400' 
WHERE image_url IS NULL AND category = 'ADICIONAIS';

UPDATE menu_items SET image_url = 'https://images.unsplash.com/photo-1561758033-d89a9ad46330?w=400' 
WHERE image_url IS NULL AND category = 'COMBOS';

-- Verificar atualizações (mostra produtos SEM imagem primeiro)
SELECT id, name, category, image_url FROM menu_items WHERE image_url IS NULL ORDER BY category, name;

-- Verificar todas as atualizações
SELECT id, name, category, 
       CASE WHEN image_url IS NULL THEN '❌ SEM IMAGEM' ELSE '✅ COM IMAGEM' END as status
FROM menu_items 
ORDER BY category, name;
