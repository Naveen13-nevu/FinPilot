-- =========================================================
-- V1: Authentication & User tables
-- =========================================================

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    first_name          VARCHAR(100)  NOT NULL,
    last_name           VARCHAR(100)  NOT NULL,
    email               VARCHAR(255)  NOT NULL UNIQUE,
    password            VARCHAR(255)  NOT NULL,
    phone_number        VARCHAR(20),
    role                VARCHAR(20)   NOT NULL DEFAULT 'ROLE_USER',
    enabled             BOOLEAN       NOT NULL DEFAULT TRUE,
    account_non_locked  BOOLEAN       NOT NULL DEFAULT TRUE,
    currency            VARCHAR(10)   NOT NULL DEFAULT 'INR',
    monthly_budget      NUMERIC(19,2) NOT NULL DEFAULT 0,
    dark_mode_enabled   BOOLEAN       NOT NULL DEFAULT FALSE,
    profile_image_url   VARCHAR(500),
    created_at          TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP     NOT NULL DEFAULT now(),
    CONSTRAINT chk_users_role CHECK (role IN ('ROLE_USER', 'ROLE_ADMIN'))
);

CREATE INDEX idx_users_email ON users (email);

CREATE TABLE refresh_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token       VARCHAR(500)  NOT NULL UNIQUE,
    user_id     UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expiry_date TIMESTAMP     NOT NULL,
    revoked     BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);

CREATE TABLE password_reset_tokens (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    token       VARCHAR(255)  NOT NULL UNIQUE,
    user_id     UUID          NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    expiry_date TIMESTAMP     NOT NULL,
    used        BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP     NOT NULL DEFAULT now()
);

CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens (user_id);
