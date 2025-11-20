-- Add password column to clients table for authentication
-- Password will be stored as BCrypt hash (60 characters)
ALTER TABLE clients ADD password VARCHAR(255) NULL;
GO

-- Add index on email for efficient lookups during authentication
CREATE INDEX idx_clients_email ON clients(email);
GO

-- Update existing users with BCrypt hashed passwords
-- BCrypt hash for 'admin123': $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
-- BCrypt hash for 'customer123': $2a$10$5LS5lc2YHqJghJJqjUHJcOeqr3YpE2JZpg2H2n4HZJ0Uz0lN8fz6K

-- Update ADMIN user
UPDATE clients
SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE cliente_id = 999;
GO

-- Update regular customers with default password
UPDATE clients
SET password = '$2a$10$5LS5lc2YHqJghJJqjUHJcOeqr3YpE2JZpg2H2n4HZJ0Uz0lN8fz6K'
WHERE role = 'CUSTOMER' AND password IS NULL;
GO

-- Make password NOT NULL after data migration
ALTER TABLE clients ALTER COLUMN password VARCHAR(255) NOT NULL;
GO
