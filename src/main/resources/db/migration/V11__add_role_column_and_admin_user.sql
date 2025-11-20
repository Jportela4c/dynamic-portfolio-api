-- Add role column to clients table
ALTER TABLE clients ADD role VARCHAR(20) NOT NULL DEFAULT 'CUSTOMER';

-- Add check constraint for role values
ALTER TABLE clients ADD CONSTRAINT chk_role_values
    CHECK (role IN ('CUSTOMER', 'ADMIN'));

-- Create index on role for query performance
CREATE INDEX idx_clients_role ON clients(role);

-- Insert ADMIN user for demo purposes
INSERT INTO clients (cliente_id, cpf, nome, email, role, data_cadastro, ativo)
VALUES (999, '00000000000', 'Admin Demo', 'admin@demo.local', 'ADMIN', GETDATE(), 1);

-- Update all existing customers to CUSTOMER role (explicit, even though default)
UPDATE clients SET role = 'CUSTOMER' WHERE cliente_id != 999;
