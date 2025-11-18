-- Create investment data cache table for persistent fallback
CREATE TABLE investment_data_cache (
    cpf VARCHAR(11) PRIMARY KEY,
    investment_data NVARCHAR(MAX) NOT NULL,
    fetched_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    expires_at DATETIME2 NOT NULL,
    CONSTRAINT chk_cache_cpf_format CHECK (cpf LIKE '[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]')
);

CREATE INDEX idx_cache_expires_at ON investment_data_cache(expires_at);
