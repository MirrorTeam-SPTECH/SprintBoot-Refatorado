-- Script para corrigir o tamanho dos campos de QR Code na tabela payments
-- Execute este script no pgAdmin antes de reiniciar o Spring Boot

-- Alterar coluna qr_code de VARCHAR(255) para TEXT
ALTER TABLE payments ALTER COLUMN qr_code TYPE TEXT;

-- Alterar coluna qr_code_base64 de VARCHAR(255) para TEXT
ALTER TABLE payments ALTER COLUMN qr_code_base64 TYPE TEXT;

-- Alterar coluna ticket_url de VARCHAR(255) para VARCHAR(500)
ALTER TABLE payments ALTER COLUMN ticket_url TYPE VARCHAR(500);

-- Verificar as alterações
SELECT 
    column_name, 
    data_type, 
    character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'payments' 
AND column_name IN ('qr_code', 'qr_code_base64', 'ticket_url')
ORDER BY column_name;
