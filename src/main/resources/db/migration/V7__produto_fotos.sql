-- =========================================================
-- Produto passa a ter uma galeria de fotos (ate 5, ver ProdutoRequest) em vez de um
-- unico link colado. Migra o que ja existia em foto_url pra nova tabela antes de
-- descartar a coluna.
-- =========================================================

CREATE TABLE produto_fotos (
    produto_id BIGINT NOT NULL REFERENCES produtos(id) ON DELETE CASCADE,
    ordem INT NOT NULL,
    url VARCHAR(1000) NOT NULL,
    PRIMARY KEY (produto_id, ordem)
);

INSERT INTO produto_fotos (produto_id, ordem, url)
SELECT id, 0, foto_url FROM produtos WHERE foto_url IS NOT NULL AND trim(foto_url) <> '';

ALTER TABLE produtos DROP COLUMN foto_url;
