-- V3: Creación de la tabla de refresh tokens para gestión de sesiones y rotación
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    token VARCHAR(512) NOT NULL UNIQUE,
    account_id UUID NOT NULL,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    replaced_by_id UUID,
    device_id VARCHAR(255),
    
    -- Foreign Key hacia la cuenta (el dueño de la sesión)
    CONSTRAINT fk_refresh_tokens_account 
        FOREIGN KEY (account_id) 
        REFERENCES accounts(id) 
        ON DELETE CASCADE,
        
    -- Foreign Key autorreferencial para la rotación (Token Rotation)
    CONSTRAINT fk_refresh_tokens_replaced_by 
        FOREIGN KEY (replaced_by_id) 
        REFERENCES refresh_tokens(id) 
        ON DELETE SET NULL
);

-- Índice para que el login/refresh vuele. Buscar por token será instantáneo.
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);

-- Índice para limpiar tokens de un usuario rápido (útil en logouts masivos)
CREATE INDEX idx_refresh_tokens_account_id ON refresh_tokens(account_id);

CREATE INDEX idx_refresh_tokens_account_device ON refresh_tokens(account_id, device_id);