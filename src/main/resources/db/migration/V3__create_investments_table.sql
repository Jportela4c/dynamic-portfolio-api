CREATE TABLE investimentos (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    cliente_id BIGINT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    valor DECIMAL(15,2) NOT NULL,
    rentabilidade DECIMAL(10,4) NOT NULL,
    data DATE NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE()
);

CREATE INDEX idx_investimentos_cliente ON investimentos(cliente_id);
CREATE INDEX idx_investimentos_data ON investimentos(data);
