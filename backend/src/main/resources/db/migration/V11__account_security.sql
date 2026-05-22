ALTER TABLE app_user
    ADD COLUMN email VARCHAR(254) NULL AFTER username,
    ADD COLUMN email_verified_at DATETIME NULL AFTER email,
    ADD COLUMN nickname VARCHAR(64) NULL AFTER email_verified_at,
    ADD COLUMN avatar_url VARCHAR(512) NULL AFTER nickname;

CREATE UNIQUE INDEX uk_app_user_email ON app_user(email);

CREATE TABLE auth_refresh_token (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash CHAR(64) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    revoked_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_ip VARCHAR(64) NULL,
    created_user_agent VARCHAR(255) NULL,
    CONSTRAINT fk_auth_refresh_token_user
        FOREIGN KEY (user_id) REFERENCES app_user(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_auth_refresh_token_user ON auth_refresh_token(user_id);
CREATE INDEX idx_auth_refresh_token_expires ON auth_refresh_token(expires_at);

CREATE TABLE pending_registration (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL,
    email VARCHAR(254) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    token_hash CHAR(64) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    consumed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_pending_registration_username ON pending_registration(username);
CREATE INDEX idx_pending_registration_email ON pending_registration(email);
CREATE INDEX idx_pending_registration_expires ON pending_registration(expires_at);

CREATE TABLE password_reset_token (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    token_hash CHAR(64) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    consumed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_ip VARCHAR(64) NULL,
    CONSTRAINT fk_password_reset_token_user
        FOREIGN KEY (user_id) REFERENCES app_user(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_password_reset_token_user ON password_reset_token(user_id);
CREATE INDEX idx_password_reset_token_expires ON password_reset_token(expires_at);

CREATE TABLE account_email_change_token (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    email VARCHAR(254) NOT NULL,
    token_hash CHAR(64) NOT NULL UNIQUE,
    expires_at DATETIME NOT NULL,
    consumed_at DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_email_change_user
        FOREIGN KEY (user_id) REFERENCES app_user(id)
        ON DELETE CASCADE
);

CREATE INDEX idx_account_email_change_token_user ON account_email_change_token(user_id);
CREATE INDEX idx_account_email_change_token_email ON account_email_change_token(email);
