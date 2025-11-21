-- Fix product types to match OFB standard taxonomy
UPDATE produtos SET tipo = 'RENDA_FIXA' WHERE tipo = 'FUNDO_RENDA_FIXA';
UPDATE produtos SET tipo = 'ACOES' WHERE tipo = 'FUNDO_ACOES';
UPDATE produtos SET tipo = 'MULTIMERCADO' WHERE tipo = 'FUNDO_MULTIMERCADO';
-- FII stays as FII (valid Brazilian product type)

-- Update check constraint to use OFB standard values
ALTER TABLE produtos DROP CONSTRAINT IF EXISTS CHK_produtos_tipo;
ALTER TABLE produtos ADD CONSTRAINT CHK_produtos_tipo
CHECK (tipo IN ('CDB', 'RDB', 'LCI', 'LCA', 'RENDA_FIXA', 'ACOES', 'MULTIMERCADO', 'CAMBIAL', 'FII', 'TESOURO_SELIC', 'TESOURO_PREFIXADO', 'TESOURO_IPCA', 'TESOURO_RENDA_MAIS', 'TESOURO_EDUCA_MAIS', 'CRI', 'CRA', 'VARIABLE_INCOME', 'POUPANCA', 'UNKNOWN'));
