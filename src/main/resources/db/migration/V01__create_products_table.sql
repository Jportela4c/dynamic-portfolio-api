CREATE TABLE produtos (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    nome VARCHAR(100) NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    rentabilidade DECIMAL(10,4) NOT NULL,
    risco VARCHAR(20) NOT NULL,
    valor_minimo DECIMAL(15,2) NOT NULL,
    prazo_minimo_meses INT NOT NULL,
    prazo_maximo_meses INT,
    perfil_adequado VARCHAR(20),
    ativo BIT DEFAULT 1,
    created_at DATETIME2 DEFAULT GETDATE()
);

CREATE INDEX idx_produtos_tipo ON produtos(tipo);
CREATE INDEX idx_produtos_perfil ON produtos(perfil_adequado);
CREATE INDEX idx_produtos_ativo ON produtos(ativo);
