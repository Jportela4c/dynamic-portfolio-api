-- Test Data Setup for Newman Tests
-- Purpose: Seed fresh database with predictable test data for API contract validation
-- Usage: Run against test database before executing Newman tests

-- Clean existing data (if any)
DELETE FROM investment_history;
DELETE FROM simulations;
DELETE FROM telemetry_records;
DELETE FROM products;
DELETE FROM users;

-- Reset identity counters (SQL Server)
DBCC CHECKIDENT ('investment_history', RESEED, 0);
DBCC CHECKIDENT ('simulations', RESEED, 0);
DBCC CHECKIDENT ('telemetry_records', RESEED, 0);
DBCC CHECKIDENT ('products', RESEED, 0);
DBCC CHECKIDENT ('users', RESEED, 0);

-- Users for authentication tests
-- Password: admin123 (bcrypt hash)
INSERT INTO users (username, password, role, active) VALUES
('admin', '$2a$10$X0b4K1dZM8h3Ov5KQ7JxV.YW3N/ZQ3q5Y0Q5Q1q5Y0Q5Q1q5Y0Q5Q1', 'ADMIN', 1),
('testuser', '$2a$10$X0b4K1dZM8h3Ov5KQ7JxV.YW3N/ZQ3q5Y0Q5Q1q5Y0Q5Q1q5Y0Q5Q1', 'USER', 1);

-- Products - Covering all product types from THE SPEC
INSERT INTO products (nome, tipo, rentabilidade, risco, valor_minimo, prazo_minimo_meses, prazo_maximo_meses, perfil_adequado, ativo) VALUES
-- Conservative products
('CDB Banco Líder 120% CDI', 'CDB', 0.12, 'Baixo', 5000.00, 6, 24, 'Conservador', 1),
('LCI Imobiliário', 'LCI', 0.095, 'Baixo', 10000.00, 12, 36, 'Conservador', 1),
('LCA Agronegócio', 'LCA', 0.09, 'Baixo', 10000.00, 12, 36, 'Conservador', 1),

-- Moderate products
('Tesouro Direto Selic', 'TESOURO_DIRETO', 0.105, 'Médio', 1000.00, 1, 60, 'Moderado', 1),
('CDB Banco Digital 130% CDI', 'CDB', 0.14, 'Médio', 10000.00, 12, 24, 'Moderado', 1),

-- Aggressive products
('Fundo Multimercado', 'FUNDO', 0.18, 'Alto', 50000.00, 6, 60, 'Agressivo', 1),
('Fundo Ações', 'FUNDO', 0.22, 'Alto', 100000.00, 12, 60, 'Agressivo', 1);

-- Customer investment history for risk profile calculation

-- Customer 1: Conservative profile
-- Low volume, low frequency, liquidity-focused
INSERT INTO investment_history (cliente_id, produto_id, produto_nome, produto_tipo, valor, data_investimento, prazo_meses) VALUES
(1, 1, 'CDB Banco Líder 120% CDI', 'CDB', 5000.00, DATEADD(month, -6, GETDATE()), 6),
(1, 2, 'LCI Imobiliário', 'LCI', 8000.00, DATEADD(month, -4, GETDATE()), 12),
(1, 3, 'LCA Agronegócio', 'LCA', 7000.00, DATEADD(month, -2, GETDATE()), 12);

-- Customer 2: Moderate profile
-- Medium volume, medium frequency, balanced
INSERT INTO investment_history (cliente_id, produto_id, produto_nome, produto_tipo, valor, data_investimento, prazo_meses) VALUES
(2, 1, 'CDB Banco Líder 120% CDI', 'CDB', 15000.00, DATEADD(month, -8, GETDATE()), 12),
(2, 4, 'Tesouro Direto Selic', 'TESOURO_DIRETO', 20000.00, DATEADD(month, -6, GETDATE()), 24),
(2, 5, 'CDB Banco Digital 130% CDI', 'CDB', 25000.00, DATEADD(month, -4, GETDATE()), 18),
(2, 2, 'LCI Imobiliário', 'LCI', 18000.00, DATEADD(month, -3, GETDATE()), 12),
(2, 6, 'Fundo Multimercado', 'FUNDO', 30000.00, DATEADD(month, -2, GETDATE()), 24);

-- Customer 3: Aggressive profile
-- High volume, high frequency, profitability-focused
INSERT INTO investment_history (cliente_id, produto_id, produto_nome, produto_tipo, valor, data_investimento, prazo_meses) VALUES
(3, 6, 'Fundo Multimercado', 'FUNDO', 80000.00, DATEADD(month, -10, GETDATE()), 36),
(3, 7, 'Fundo Ações', 'FUNDO', 120000.00, DATEADD(month, -9, GETDATE()), 48),
(3, 5, 'CDB Banco Digital 130% CDI', 'CDB', 50000.00, DATEADD(month, -7, GETDATE()), 24),
(3, 6, 'Fundo Multimercado', 'FUNDO', 90000.00, DATEADD(month, -5, GETDATE()), 24),
(3, 7, 'Fundo Ações', 'FUNDO', 150000.00, DATEADD(month, -4, GETDATE()), 36),
(3, 6, 'Fundo Multimercado', 'FUNDO', 100000.00, DATEADD(month, -2, GETDATE()), 24),
(3, 7, 'Fundo Ações', 'FUNDO', 180000.00, DATEADD(month, -1, GETDATE()), 48);

-- Sample simulations for testing GET /simulacoes endpoint
INSERT INTO simulations (cliente_id, valor_inicial, prazo_meses, tipo_produto, produto_id, rentabilidade, valor_final, lucro, data_simulacao) VALUES
(1, 10000.00, 12, 'CDB', 1, 0.12, 11200.00, 1200.00, GETDATE()),
(1, 15000.00, 18, 'LCI', 2, 0.095, 16425.00, 1425.00, GETDATE()),
(2, 25000.00, 24, 'TESOURO_DIRETO', 4, 0.105, 27625.00, 2625.00, GETDATE()),
(3, 100000.00, 36, 'FUNDO', 6, 0.18, 118000.00, 18000.00, GETDATE());

-- Sample telemetry records for testing GET /telemetria endpoint
INSERT INTO telemetry_records (servico, endpoint, tempo_resposta_ms, data_hora, status_code) VALUES
('SimulationService', '/simular-investimento', 245, DATEADD(hour, -2, GETDATE()), 200),
('SimulationService', '/simular-investimento', 189, DATEADD(hour, -1, GETDATE()), 200),
('SimulationService', '/simular-investimento', 312, GETDATE(), 200),
('RiskProfileService', '/perfil-risco/1', 156, DATEADD(hour, -3, GETDATE()), 200),
('RiskProfileService', '/perfil-risco/2', 178, DATEADD(hour, -2, GETDATE()), 200),
('RiskProfileService', '/perfil-risco/3', 145, DATEADD(hour, -1, GETDATE()), 200),
('ProductService', '/produtos-recomendados/Conservador', 89, DATEADD(hour, -4, GETDATE()), 200),
('ProductService', '/produtos-recomendados/Moderado', 92, DATEADD(hour, -3, GETDATE()), 200),
('ProductService', '/produtos-recomendados/Agressivo', 87, DATEADD(hour, -2, GETDATE()), 200);

-- Verify data was inserted correctly
SELECT 'Users' AS TableName, COUNT(*) AS RowCount FROM users
UNION ALL
SELECT 'Products', COUNT(*) FROM products
UNION ALL
SELECT 'Investment History', COUNT(*) FROM investment_history
UNION ALL
SELECT 'Simulations', COUNT(*) FROM simulations
UNION ALL
SELECT 'Telemetry Records', COUNT(*) FROM telemetry_records;

-- Display summary for verification
PRINT 'Test data setup completed successfully!';
PRINT '';
PRINT 'Summary:';
PRINT '- 2 users (admin, testuser)';
PRINT '- 7 products (CDB, LCI, LCA, Tesouro Direto, Fundos)';
PRINT '- 15 investment records (3 clients with different profiles)';
PRINT '- 4 sample simulations';
PRINT '- 9 telemetry records';
PRINT '';
PRINT 'Customer profiles:';
PRINT '  Customer 1: Conservative (low volume, low frequency)';
PRINT '  Customer 2: Moderate (medium volume, balanced)';
PRINT '  Customer 3: Aggressive (high volume, high frequency)';
