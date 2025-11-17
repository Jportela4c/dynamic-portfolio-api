-- Test Data Setup for Newman Tests
-- Purpose: Add additional test data for Newman API contract validation
-- Note: Products are already seeded by V5__seed_sample_products.sql migration

-- Clean existing test simulation and telemetry data
DELETE FROM simulacoes;
DELETE FROM telemetria;

-- Add investment history for customers 1, 2, 3 (for risk profile calculation)
-- Customer 1: Conservative profile (low volume, low frequency)
INSERT INTO investimentos (cliente_id, tipo, valor, rentabilidade, data) VALUES
(1, 'CDB', 5000.00, 0.12, '2025-01-15'),
(1, 'LCI', 8000.00, 0.10, '2025-03-10'),
(1, 'LCA', 7000.00, 0.11, '2025-06-20');

-- Customer 2: Moderate profile (medium volume, balanced)
INSERT INTO investimentos (cliente_id, tipo, valor, rentabilidade, data) VALUES
(2, 'CDB', 15000.00, 0.15, '2025-02-01'),
(2, 'TESOURO_DIRETO', 20000.00, 0.13, '2025-04-15'),
(2, 'CDB', 25000.00, 0.15, '2025-05-10'),
(2, 'LCI', 18000.00, 0.13, '2025-07-01'),
(2, 'FUNDO_MULTIMERCADO', 30000.00, 0.20, '2025-08-20');

-- Customer 3: Aggressive profile (high volume, high frequency)
INSERT INTO investimentos (cliente_id, tipo, valor, rentabilidade, data) VALUES
(3, 'FUNDO_MULTIMERCADO', 80000.00, 0.20, '2025-01-05'),
(3, 'FUNDO_ACOES', 120000.00, 0.25, '2025-02-10'),
(3, 'CDB', 50000.00, 0.18, '2025-03-15'),
(3, 'FUNDO_MULTIMERCADO', 90000.00, 0.20, '2025-05-20'),
(3, 'FUNDO_ACOES', 150000.00, 0.25, '2025-07-10'),
(3, 'FUNDO_MULTIMERCADO', 100000.00, 0.20, '2025-09-01'),
(3, 'FUNDO_ACOES', 180000.00, 0.25, '2025-10-15');

-- Sample simulations for testing GET /simulacoes endpoint
-- Schema: id, cliente_id, produto_id, produto_nome, valor_investido, valor_final, prazo_meses, data_simulacao
INSERT INTO simulacoes (cliente_id, produto_id, produto_nome, valor_investido, valor_final, prazo_meses, data_simulacao) VALUES
(1, 1, 'CDB Caixa 2026', 10000.00, 11200.00, 12, GETDATE()),
(1, 2, 'LCI Premium', 15000.00, 16500.00, 18, GETDATE()),
(2, 4, 'Tesouro Selic 2027', 25000.00, 28250.00, 24, GETDATE()),
(3, 9, 'Fundo Multimercado Agressivo', 100000.00, 120000.00, 36, GETDATE());

-- Sample telemetry records for testing GET /telemetria endpoint
-- Schema: id, servico, tempo_resposta_ms, timestamp, sucesso, codigo_status
INSERT INTO telemetria (servico, tempo_resposta_ms, timestamp, sucesso, codigo_status) VALUES
('SimulationService', 245, DATEADD(hour, -2, GETDATE()), 1, 200),
('SimulationService', 189, DATEADD(hour, -1, GETDATE()), 1, 200),
('SimulationService', 312, GETDATE(), 1, 200),
('RiskProfileService', 156, DATEADD(hour, -3, GETDATE()), 1, 200),
('RiskProfileService', 178, DATEADD(hour, -2, GETDATE()), 1, 200),
('RiskProfileService', 145, DATEADD(hour, -1, GETDATE()), 1, 200),
('ProductService', 89, DATEADD(hour, -4, GETDATE()), 1, 200),
('ProductService', 92, DATEADD(hour, -3, GETDATE()), 1, 200),
('ProductService', 87, DATEADD(hour, -2, GETDATE()), 1, 200);

-- Verify data was inserted correctly
SELECT 'Investment History' AS TableName, COUNT(*) AS RecordCount FROM investimentos WHERE cliente_id IN (1, 2, 3)
UNION ALL
SELECT 'Simulations' AS TableName, COUNT(*) AS RecordCount FROM simulacoes WHERE cliente_id IN (1, 2, 3)
UNION ALL
SELECT 'Telemetry Records' AS TableName, COUNT(*) AS RecordCount FROM telemetria;

-- Display summary
SELECT 'Test data setup completed!' AS Message;
SELECT 'Added:' AS Summary UNION ALL
SELECT '- 15 investment records for customers 1, 2, 3' UNION ALL
SELECT '- 4 sample simulations' UNION ALL
SELECT '- 9 telemetry records';
