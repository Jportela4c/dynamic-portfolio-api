-- Add role column to clients table with default value
ALTER TABLE clients ADD role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER';
GO

-- Add check constraint for role values
ALTER TABLE clients WITH NOCHECK ADD CONSTRAINT chk_role_values
    CHECK (role IN ('CUSTOMER', 'ADMIN'));
GO

-- Create index on role for query performance
CREATE INDEX idx_clients_role ON clients(role);
GO

-- Insert ADMIN user for demo purposes (requires IDENTITY_INSERT)
SET IDENTITY_INSERT clients ON;
INSERT INTO clients (cliente_id, cpf, nome, email, role, data_cadastro, ativo)
VALUES (999, '00000000000', 'Admin Demo', 'admin@demo.local', 'ADMIN', GETDATE(), 1);
SET IDENTITY_INSERT clients OFF;
GO
