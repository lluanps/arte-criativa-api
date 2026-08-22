-- =========================================================
-- Ficha tecnica passa a ter, alem do custo de materia-prima (insumo), campos opcionais
-- de custo de mao de obra e embalagem/outros custos indiretos, por unidade produzida.
-- Default 0 -- fichas ja cadastradas continuam com o mesmo preco sugerido/margem de
-- antes, ate o usuario preencher esses campos.
-- =========================================================

ALTER TABLE receitas
    ADD COLUMN custo_mao_de_obra NUMERIC(12,2) NOT NULL DEFAULT 0,
    ADD COLUMN custo_embalagem_outros NUMERIC(12,2) NOT NULL DEFAULT 0;
