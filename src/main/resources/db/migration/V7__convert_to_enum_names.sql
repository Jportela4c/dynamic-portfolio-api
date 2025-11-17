-- Convert existing database values to enum constant names for @Enumerated(STRING)

-- Update produtos table (tipo column)
UPDATE produtos SET tipo = 'TESOURO_DIRETO' WHERE tipo = 'Tesouro Direto';
UPDATE produtos SET tipo = 'FUNDO_RENDA_FIXA' WHERE tipo = 'Fundo Renda Fixa';
UPDATE produtos SET tipo = 'FUNDO_MULTIMERCADO' WHERE tipo = 'Fundo Multimercado';
UPDATE produtos SET tipo = 'FUNDO_ACOES' WHERE tipo = 'Fundo Ações';

-- Update investimentos table (tipo column)
UPDATE investimentos SET tipo = 'TESOURO_DIRETO' WHERE tipo = 'Tesouro Direto';
UPDATE investimentos SET tipo = 'FUNDO_RENDA_FIXA' WHERE tipo = 'Fundo Renda Fixa';
UPDATE investimentos SET tipo = 'FUNDO_MULTIMERCADO' WHERE tipo = 'Fundo Multimercado';
UPDATE investimentos SET tipo = 'FUNDO_ACOES' WHERE tipo = 'Fundo Ações';

-- Drop old CHECK constraints
ALTER TABLE produtos DROP CONSTRAINT IF EXISTS CK_produtos_tipo;
ALTER TABLE investimentos DROP CONSTRAINT IF EXISTS CK_investimentos_tipo;

-- Add new CHECK constraints with enum constant names
ALTER TABLE produtos
ADD CONSTRAINT CK_produtos_tipo
CHECK (tipo IN ('CDB', 'LCI', 'LCA', 'TESOURO_DIRETO', 'FUNDO_RENDA_FIXA', 'FUNDO_MULTIMERCADO', 'FUNDO_ACOES', 'FII'));

ALTER TABLE investimentos
ADD CONSTRAINT CK_investimentos_tipo
CHECK (tipo IN ('CDB', 'LCI', 'LCA', 'TESOURO_DIRETO', 'FUNDO_RENDA_FIXA', 'FUNDO_MULTIMERCADO', 'FUNDO_ACOES', 'FII'));
