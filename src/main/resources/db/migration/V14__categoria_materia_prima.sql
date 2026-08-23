-- =========================================================
-- Categoria de matéria-prima (ex: ceras, pavios, embalagens, adesivos) -- separada da
-- categoria de PRODUTO (categorias) de propósito: assuntos diferentes, e a de produto
-- carrega campos (preço de mercado) que não fazem sentido pra um insumo.
-- =========================================================

CREATE TABLE categorias_materia_prima (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL UNIQUE,
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);

ALTER TABLE materias_primas
    ADD COLUMN categoria_id BIGINT REFERENCES categorias_materia_prima(id);
