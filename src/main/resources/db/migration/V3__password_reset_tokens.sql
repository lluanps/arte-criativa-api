-- =========================================================
-- Recuperação de senha: tokens temporários de reset, um por
-- pedido de "esqueci minha senha". Token expira em 1h e só
-- pode ser usado uma vez (usado_em marcado na hora do uso).
-- =========================================================

CREATE TABLE password_reset_tokens (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id) ON DELETE CASCADE,
    token VARCHAR(100) NOT NULL UNIQUE,
    expira_em TIMESTAMP NOT NULL,
    usado_em TIMESTAMP,
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens(token);
