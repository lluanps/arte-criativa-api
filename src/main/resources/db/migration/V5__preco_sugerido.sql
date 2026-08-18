-- =========================================================
-- Preço sugerido: margem desejada por produto (usada junto com
-- o custo de produção da ficha técnica) + faixa de referência de
-- mercado por categoria (preenchida manualmente via pesquisa
-- periódica, não é busca automática em tempo real).
-- =========================================================

ALTER TABLE produtos ADD COLUMN margem_desejada_percentual NUMERIC(6,2);

ALTER TABLE categorias ADD COLUMN preco_mercado_min NUMERIC(12,2);
ALTER TABLE categorias ADD COLUMN preco_mercado_max NUMERIC(12,2);
ALTER TABLE categorias ADD COLUMN preco_mercado_atualizado_em TIMESTAMP;
