-- Add CHECK constraints to enforce enum-like behavior in SQL Server

-- Constraint for product type (tipo_produto)
ALTER TABLE produtos
ADD CONSTRAINT CK_produtos_tipo_produto
CHECK (tipo_produto IN ('CDB', 'LCI', 'LCA', 'Tesouro Direto', 'Fundo'));

-- Constraint for risk level (nivel_risco)
ALTER TABLE produtos
ADD CONSTRAINT CK_produtos_nivel_risco
CHECK (nivel_risco IN ('Baixo', 'Médio', 'Alto'));

-- Constraint for risk profile (perfil_risco)
ALTER TABLE produtos
ADD CONSTRAINT CK_produtos_perfil_risco
CHECK (perfil_risco IN ('Conservador', 'Moderado', 'Agressivo'));

-- Constraint for product type in simulations table
ALTER TABLE simulacoes
ADD CONSTRAINT CK_simulacoes_tipo_produto
CHECK (tipo_produto IN ('CDB', 'LCI', 'LCA', 'Tesouro Direto', 'Fundo'));

-- Constraint for product type in investments table
ALTER TABLE investimentos
ADD CONSTRAINT CK_investimentos_tipo_produto
CHECK (tipo_produto IN ('CDB', 'LCI', 'LCA', 'Tesouro Direto', 'Fundo'));
