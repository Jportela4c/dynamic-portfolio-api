-- Conservative Products
INSERT INTO produtos (nome, tipo, rentabilidade, risco, valor_minimo, prazo_minimo_meses, prazo_maximo_meses, perfil_adequado, ativo)
VALUES
('CDB Caixa 2026', 'CDB', 0.12, 'Baixo', 1000.00, 6, 60, 'Conservador', 1),
('LCI Premium', 'LCI', 0.10, 'Baixo', 5000.00, 12, 36, 'Conservador', 1),
('LCA Agro Invest', 'LCA', 0.11, 'Baixo', 3000.00, 12, 48, 'Conservador', 1),
('Tesouro Selic 2027', 'Tesouro Direto', 0.13, 'Baixo', 100.00, 3, 120, 'Conservador', 1);

-- Moderate Products
INSERT INTO produtos (nome, tipo, rentabilidade, risco, valor_minimo, prazo_minimo_meses, prazo_maximo_meses, perfil_adequado, ativo)
VALUES
('CDB Prefixado Plus', 'CDB', 0.15, 'Médio', 2000.00, 12, 36, 'Moderado', 1),
('Fundo Renda Fixa Moderado', 'Fundo', 0.14, 'Médio', 1000.00, 6, NULL, 'Moderado', 1),
('Tesouro IPCA+ 2035', 'Tesouro Direto', 0.16, 'Médio', 100.00, 12, 180, 'Moderado', 1),
('LCI Moderada', 'LCI', 0.13, 'Médio', 3000.00, 12, 36, 'Moderado', 1);

-- Aggressive Products
INSERT INTO produtos (nome, tipo, rentabilidade, risco, valor_minimo, prazo_minimo_meses, prazo_maximo_meses, perfil_adequado, ativo)
VALUES
('Fundo Multimercado Agressivo', 'Fundo', 0.20, 'Alto', 5000.00, 12, NULL, 'Agressivo', 1),
('CDB Alto Rendimento', 'CDB', 0.18, 'Alto', 10000.00, 24, 60, 'Agressivo', 1),
('Fundo Ações Brasil', 'Fundo', 0.25, 'Alto', 2000.00, 6, NULL, 'Agressivo', 1),
('Fundo Imobiliário FII Premium', 'Fundo', 0.22, 'Alto', 1000.00, 12, NULL, 'Agressivo', 1);

-- Sample client investments for risk profiling
INSERT INTO investimentos (cliente_id, tipo, valor, rentabilidade, data)
VALUES
(123, 'CDB', 5000.00, 0.12, '2025-01-15'),
(123, 'Fundo Multimercado', 3000.00, 0.08, '2025-03-10'),
(123, 'LCI', 7000.00, 0.10, '2025-06-20'),
(456, 'CDB', 15000.00, 0.15, '2025-02-01'),
(456, 'Fundo', 20000.00, 0.18, '2025-04-15'),
(456, 'Tesouro Direto', 10000.00, 0.13, '2025-07-01'),
(789, 'Tesouro Selic', 500.00, 0.10, '2025-05-10'),
(789, 'CDB', 1000.00, 0.11, '2025-08-20');
