-- =========================================================
-- Caderno de ideias: anotacoes soltas de inspiracao, opcionalmente com
-- imagens, tags e um vinculo leve (nao bloqueante) com um produto existente.
-- =========================================================

CREATE TABLE ideias (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    corpo TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'IDEIA_SOLTA'
        CHECK (status IN ('IDEIA_SOLTA','EM_TESTE','VIROU_PRODUTO','DESCARTADA')),
    favorita BOOLEAN NOT NULL DEFAULT false,
    produto_relacionado_id BIGINT REFERENCES produtos(id),
    criado_em TIMESTAMP NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE ideia_tags (
    ideia_id BIGINT NOT NULL REFERENCES ideias(id) ON DELETE CASCADE,
    ordem INT NOT NULL,
    tag VARCHAR(50) NOT NULL,
    PRIMARY KEY (ideia_id, ordem)
);

CREATE TABLE ideia_fotos (
    ideia_id BIGINT NOT NULL REFERENCES ideias(id) ON DELETE CASCADE,
    ordem INT NOT NULL,
    url VARCHAR(1000) NOT NULL,
    PRIMARY KEY (ideia_id, ordem)
);

CREATE INDEX idx_ideias_status ON ideias(status);
CREATE INDEX idx_ideias_produto_relacionado_id ON ideias(produto_relacionado_id);
