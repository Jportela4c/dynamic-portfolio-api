CREATE TABLE simulacoes (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    cliente_id BIGINT NOT NULL,
    produto_id BIGINT NOT NULL,
    produto_nome VARCHAR(100) NOT NULL,
    valor_investido DECIMAL(15,2) NOT NULL,
    valor_final DECIMAL(15,2) NOT NULL,
    prazo_meses INT NOT NULL,
    data_simulacao DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (produto_id) REFERENCES produtos(id)
);

CREATE INDEX idx_simulacoes_cliente ON simulacoes(cliente_id);
CREATE INDEX idx_simulacoes_data ON simulacoes(data_simulacao);
CREATE INDEX idx_simulacoes_produto ON simulacoes(produto_id);
