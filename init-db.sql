-- Create database if it doesn't exist
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'portfoliodb')
BEGIN
    CREATE DATABASE portfoliodb;
END
GO

USE portfoliodb;
GO
