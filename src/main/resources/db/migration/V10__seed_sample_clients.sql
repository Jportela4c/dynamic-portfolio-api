-- Sample clients for testing (CPFs match OFB mock server data)
-- These 5 clients have mock investments distributed in OFB mock server
INSERT INTO clients (cpf, nome, email, ativo)
VALUES
('73677831148', 'João Silva', 'joao.silva@example.com', 1),
('96846726756', 'Maria Santos', 'maria.santos@example.com', 1),
('17418143834', 'Pedro Costa', 'pedro.costa@example.com', 1),
('25791178816', 'Ana Oliveira', 'ana.oliveira@example.com', 1),
('74979890814', 'Carlos Lima', 'carlos.lima@example.com', 1);
