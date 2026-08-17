-- =========================================================
-- Cadastros: Categoria (produto), Canal de venda, Cliente
-- Categoria/canal deixam de ser texto livre em produtos/vendas e viram
-- FK pra essas tabelas novas. Cliente vira FK em vendas (antes era so
-- um nome digitado). Produto e materia-prima ganham volume_ml opcional.
-- =========================================================

CREATE TABLE categorias (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE canais_venda (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE clientes (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    telefone VARCHAR(30),
    email VARCHAR(150),
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------- PRODUTOS ----------

ALTER TABLE produtos ADD COLUMN categoria_id BIGINT REFERENCES categorias(id);
ALTER TABLE produtos ADD COLUMN volume_ml NUMERIC(10,2);

-- Backfill: cada valor distinto de categoria (texto livre) existente vira uma linha em
-- categorias, e os produtos são religados por FK antes da coluna antiga ser descartada.
INSERT INTO categorias (nome)
SELECT DISTINCT trim(categoria) FROM produtos
WHERE categoria IS NOT NULL AND trim(categoria) <> ''
ON CONFLICT (nome) DO NOTHING;

UPDATE produtos p
SET categoria_id = c.id
FROM categorias c
WHERE p.categoria IS NOT NULL AND trim(p.categoria) <> '' AND c.nome = trim(p.categoria);

ALTER TABLE produtos DROP COLUMN categoria;

CREATE INDEX idx_produtos_categoria_id ON produtos(categoria_id);

-- ---------- MATERIAS-PRIMAS ----------

ALTER TABLE materias_primas ADD COLUMN volume_ml NUMERIC(10,2);

-- ---------- VENDAS ----------

ALTER TABLE vendas ADD COLUMN canal_id BIGINT REFERENCES canais_venda(id);
ALTER TABLE vendas ADD COLUMN cliente_id BIGINT REFERENCES clientes(id);

-- Backfill: mesmo raciocínio acima, para canal (texto livre -> canais_venda) e
-- cliente_nome (texto livre -> clientes, um cadastro mínimo por nome distinto).
INSERT INTO canais_venda (nome)
SELECT DISTINCT trim(canal) FROM vendas
WHERE canal IS NOT NULL AND trim(canal) <> ''
ON CONFLICT (nome) DO NOTHING;

UPDATE vendas v
SET canal_id = cv.id
FROM canais_venda cv
WHERE v.canal IS NOT NULL AND trim(v.canal) <> '' AND cv.nome = trim(v.canal);

INSERT INTO clientes (nome)
SELECT DISTINCT trim(cliente_nome) FROM vendas
WHERE cliente_nome IS NOT NULL AND trim(cliente_nome) <> '';

UPDATE vendas v
SET cliente_id = cl.id
FROM clientes cl
WHERE v.cliente_nome IS NOT NULL AND trim(v.cliente_nome) <> '' AND cl.nome = trim(v.cliente_nome)
  AND cl.id = (
      SELECT MIN(id) FROM clientes WHERE nome = trim(v.cliente_nome)
  );

ALTER TABLE vendas DROP COLUMN canal;
ALTER TABLE vendas DROP COLUMN cliente_nome;

CREATE INDEX idx_vendas_canal_id ON vendas(canal_id);
CREATE INDEX idx_vendas_cliente_id ON vendas(cliente_id);
