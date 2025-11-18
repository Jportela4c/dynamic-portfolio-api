-- Create clients table for identity mapping
CREATE TABLE clients (
    cliente_id BIGINT PRIMARY KEY IDENTITY(1,1),
    cpf VARCHAR(11) NOT NULL UNIQUE,
    nome VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    data_cadastro DATETIME2 NOT NULL DEFAULT GETDATE(),
    data_atualizacao DATETIME2,
    ativo BIT NOT NULL DEFAULT 1,
    CONSTRAINT chk_cpf_format CHECK (cpf LIKE '[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]')
);

CREATE INDEX idx_clients_cpf ON clients(cpf);
