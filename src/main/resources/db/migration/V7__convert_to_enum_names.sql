-- Convert existing database values to enum constant names for @Enumerated(STRING)

-- Update produtos table
UPDATE produtos SET tipo_produto = 'TESOURO_DIRETO' WHERE tipo_produto = 'Tesouro Direto';
UPDATE produtos SET tipo_produto = 'FUNDO_RENDA_FIXA' WHERE tipo_produto = 'Fundo Renda Fixa';
UPDATE produtos SET tipo_produto = 'FUNDO_MULTIMERCADO' WHERE tipo_produto = 'Fundo Multimercado';
UPDATE produtos SET tipo_produto = 'FUNDO_ACOES' WHERE tipo_produto = 'Fundo Ações';

-- Update simulacoes table
UPDATE simulacoes SET tipo_produto = 'TESOURO_DIRETO' WHERE tipo_produto = 'Tesouro Direto';
UPDATE simulacoes SET tipo_produto = 'FUNDO_RENDA_FIXA' WHERE tipo_produto = 'Fundo Renda Fixa';
UPDATE simulacoes SET tipo_produto = 'FUNDO_MULTIMERCADO' WHERE tipo_produto = 'Fundo Multimercado';
UPDATE simulacoes SET tipo_produto = 'FUNDO_ACOES' WHERE tipo_produto = 'Fundo Ações';

-- Update investimentos table
UPDATE investimentos SET tipo_produto = 'TESOURO_DIRETO' WHERE tipo_produto = 'Tesouro Direto';
UPDATE investimentos SET tipo_produto = 'FUNDO_RENDA_FIXA' WHERE tipo_produto = 'Fundo Renda Fixa';
UPDATE investimentos SET tipo_produto = 'FUNDO_MULTIMERCADO' WHERE tipo_produto = 'Fundo Multimercado';
UPDATE investimentos SET tipo_produto = 'FUNDO_ACOES' WHERE tipo_produto = 'Fundo Ações';

-- Drop old CHECK constraints
ALTER TABLE produtos DROP CONSTRAINT IF EXISTS CK_produtos_tipo_produto;
ALTER TABLE simulacoes DROP CONSTRAINT IF EXISTS CK_simulacoes_tipo_produto;
ALTER TABLE investimentos DROP CONSTRAINT IF EXISTS CK_investimentos_tipo_produto;

-- Add new CHECK constraints with enum constant names
ALTER TABLE produtos
ADD CONSTRAINT CK_produtos_tipo_produto
CHECK (tipo_produto IN ('CDB', 'LCI', 'LCA', 'TESOURO_DIRETO', 'FUNDO_RENDA_FIXA', 'FUNDO_MULTIMERCADO', 'FUNDO_ACOES', 'FII'));

ALTER TABLE simulacoes
ADD CONSTRAINT CK_simulacoes_tipo_produto
CHECK (tipo_produto IN ('CDB', 'LCI', 'LCA', 'TESOURO_DIRETO', 'FUNDO_RENDA_FIXA', 'FUNDO_MULTIMERCADO', 'FUNDO_ACOES', 'FII'));

ALTER TABLE investimentos
ADD CONSTRAINT CK_investimentos_tipo_produto
CHECK (tipo_produto IN ('CDB', 'LCI', 'LCA', 'TESOURO_DIRETO', 'FUNDO_RENDA_FIXA', 'FUNDO_MULTIMERCADO', 'FUNDO_ACOES', 'FII'));
