CREATE TABLE telemetria (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    servico VARCHAR(100) NOT NULL,
    tempo_resposta_ms INT NOT NULL,
    timestamp DATETIME2 DEFAULT GETDATE(),
    sucesso BIT DEFAULT 1,
    codigo_status INT
);

CREATE INDEX idx_telemetria_servico ON telemetria(servico);
CREATE INDEX idx_telemetria_timestamp ON telemetria(timestamp);
