-- Add CHECK constraints for enum values and business rules

-- TipoProduto constraints
ALTER TABLE produtos ADD CONSTRAINT chk_produtos_tipo
    CHECK (tipo IN ('CDB', 'LCI', 'LCA', 'TESOURO_DIRETO',
                    'FUNDO_RENDA_FIXA', 'FUNDO_MULTIMERCADO',
                    'FUNDO_ACOES', 'FII'));

-- PerfilRisco constraint
ALTER TABLE produtos ADD CONSTRAINT chk_produtos_perfil
    CHECK (perfil_adequado IN ('CONSERVADOR', 'MODERADO', 'AGRESSIVO'));

-- Business rule constraints
ALTER TABLE produtos ADD CONSTRAINT chk_produtos_valor_minimo
    CHECK (valor_minimo > 0);

ALTER TABLE produtos ADD CONSTRAINT chk_produtos_prazo
    CHECK (prazo_minimo_meses > 0
       AND (prazo_maximo_meses IS NULL OR prazo_maximo_meses >= prazo_minimo_meses));

ALTER TABLE produtos ADD CONSTRAINT chk_produtos_rentabilidade
    CHECK (rentabilidade >= 0);

ALTER TABLE simulacoes ADD CONSTRAINT chk_simulacoes_valores
    CHECK (valor_investido > 0 AND valor_final > 0);

ALTER TABLE simulacoes ADD CONSTRAINT chk_simulacoes_prazo
    CHECK (prazo_meses > 0);
