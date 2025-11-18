-- Add CHECK constraints to enforce enum-like behavior in SQL Server

-- Constraint for product type (tipo column in produtos table)
ALTER TABLE produtos
ADD CONSTRAINT CK_produtos_tipo
CHECK (tipo IN ('CDB', 'LCI', 'LCA', 'TESOURO_DIRETO', 'FUNDO_RENDA_FIXA', 'FUNDO_MULTIMERCADO', 'FUNDO_ACOES', 'FII'));

-- Constraint for risk level (risco column)
ALTER TABLE produtos
ADD CONSTRAINT CK_produtos_risco
CHECK (risco IN ('Baixo', 'Médio', 'Alto'));

-- Constraint for risk profile (perfil_adequado column)
ALTER TABLE produtos
ADD CONSTRAINT CK_produtos_perfil_adequado
CHECK (perfil_adequado IN ('Conservador', 'Moderado', 'Agressivo'));
