-- Tabla principal de cuentas
CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    version BIGINT NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE,
    last_login_at TIMESTAMP WITH TIME ZONE
);

-- Tabla para la colección de roles (ElementCollection)
CREATE TABLE account_roles (
    account_id UUID NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT fk_account_roles_account FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE CASCADE
);

-- Índice para mejorar las búsquedas de roles
CREATE INDEX idx_account_roles_account_id ON account_roles(account_id);