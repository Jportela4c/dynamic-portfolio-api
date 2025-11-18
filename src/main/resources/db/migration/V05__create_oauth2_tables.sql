-- OAuth2 Authorization Server tables
-- Based on Spring Authorization Server schema

CREATE TABLE oauth2_authorization (
    id VARCHAR(100) NOT NULL,
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name VARCHAR(200) NOT NULL,
    authorization_grant_type VARCHAR(100) NOT NULL,
    authorized_scopes VARCHAR(1000),
    attributes NVARCHAR(MAX),
    state VARCHAR(500),
    authorization_code_value NVARCHAR(MAX),
    authorization_code_issued_at DATETIME2,
    authorization_code_expires_at DATETIME2,
    authorization_code_metadata NVARCHAR(MAX),
    access_token_value NVARCHAR(MAX),
    access_token_issued_at DATETIME2,
    access_token_expires_at DATETIME2,
    access_token_metadata NVARCHAR(MAX),
    access_token_type VARCHAR(100),
    access_token_scopes VARCHAR(1000),
    oidc_id_token_value NVARCHAR(MAX),
    oidc_id_token_issued_at DATETIME2,
    oidc_id_token_expires_at DATETIME2,
    oidc_id_token_metadata NVARCHAR(MAX),
    refresh_token_value NVARCHAR(MAX),
    refresh_token_issued_at DATETIME2,
    refresh_token_expires_at DATETIME2,
    refresh_token_metadata NVARCHAR(MAX),
    user_code_value NVARCHAR(MAX),
    user_code_issued_at DATETIME2,
    user_code_expires_at DATETIME2,
    user_code_metadata NVARCHAR(MAX),
    device_code_value NVARCHAR(MAX),
    device_code_issued_at DATETIME2,
    device_code_expires_at DATETIME2,
    device_code_metadata NVARCHAR(MAX),
    CONSTRAINT pk_oauth2_authorization PRIMARY KEY (id)
);

CREATE TABLE oauth2_authorization_consent (
    registered_client_id VARCHAR(100) NOT NULL,
    principal_name VARCHAR(200) NOT NULL,
    authorities VARCHAR(1000) NOT NULL,
    CONSTRAINT pk_oauth2_authorization_consent PRIMARY KEY (registered_client_id, principal_name)
);

CREATE TABLE oauth2_registered_client (
    id VARCHAR(100) NOT NULL,
    client_id VARCHAR(100) NOT NULL,
    client_id_issued_at DATETIME2 DEFAULT CURRENT_TIMESTAMP NOT NULL,
    client_secret VARCHAR(200),
    client_secret_expires_at DATETIME2,
    client_name VARCHAR(200) NOT NULL,
    client_authentication_methods VARCHAR(1000) NOT NULL,
    authorization_grant_types VARCHAR(1000) NOT NULL,
    redirect_uris VARCHAR(1000),
    post_logout_redirect_uris VARCHAR(1000),
    scopes VARCHAR(1000) NOT NULL,
    client_settings NVARCHAR(MAX) NOT NULL,
    token_settings NVARCHAR(MAX) NOT NULL,
    CONSTRAINT pk_oauth2_registered_client PRIMARY KEY (id)
);

CREATE UNIQUE INDEX idx_oauth2_registered_client_client_id ON oauth2_registered_client(client_id);
