-- Add password column to clients table for authentication
-- Password will be stored as BCrypt hash (60 characters)
ALTER TABLE clients ADD password VARCHAR(255) NULL;
GO

-- Add index on email for efficient lookups during authentication
CREATE INDEX idx_clients_email ON clients(email);
GO

-- Update existing users with BCrypt hashed passwords
-- BCrypt hash for 'admin123': $2a$10$x73ekZHKMsv/usFBfEmJ2eEWqcj4Zjw/AkgK/laO4qmYVAMqjB8la
-- BCrypt hash for 'customer123': $2a$10$iLey2CKtqr1.OwfxAkTUQ.6B2qGJzwsWfDvz2FKBfTnP05dbGrt7m

-- Update ADMIN user
UPDATE clients
SET password = '$2a$10$x73ekZHKMsv/usFBfEmJ2eEWqcj4Zjw/AkgK/laO4qmYVAMqjB8la'
WHERE cliente_id = 999;
GO

-- Update regular customers with default password
UPDATE clients
SET password = '$2a$10$iLey2CKtqr1.OwfxAkTUQ.6B2qGJzwsWfDvz2FKBfTnP05dbGrt7m'
WHERE role = 'CUSTOMER' AND password IS NULL;
GO

-- Make password NOT NULL after data migration
ALTER TABLE clients ALTER COLUMN password VARCHAR(255) NOT NULL;
GO
