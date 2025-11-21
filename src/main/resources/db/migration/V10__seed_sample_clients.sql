-- Sample clients for testing (CPFs match OFB mock server data)
-- These 5 clients have mock investments distributed in OFB mock server
INSERT INTO clients (cpf, nome, email, ativo)
VALUES
('12345678901', 'João Silva', 'joao.silva@example.com', 1),
('98765432109', 'Maria Santos', 'maria.santos@example.com', 1),
('11122233344', 'Pedro Costa', 'pedro.costa@example.com', 1),
('55566677788', 'Ana Oliveira', 'ana.oliveira@example.com', 1),
('99988877766', 'Carlos Lima', 'carlos.lima@example.com', 1);
