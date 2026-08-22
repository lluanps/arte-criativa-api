-- =========================================================
-- Item de receita passa a ter sua propria unidade de medida (pode ser diferente da
-- cadastrada na materia-prima, ex: receita em "g" com materia-prima em "kg" -- o backend
-- converte automaticamente). Nao mexe em materias_primas.unidade_medida -- continua
-- texto livre, sem quebrar cadastros existentes.
--
-- Para itens ja existentes, preenche com a MESMA unidade que a materia-prima tem hoje --
-- ou seja, nenhuma conversao entra em jogo pra receita nenhuma ja cadastrada, o
-- comportamento fica identico ao de antes desta migration.
-- =========================================================

ALTER TABLE receita_itens ADD COLUMN unidade_medida VARCHAR(20);

UPDATE receita_itens ri
SET unidade_medida = mp.unidade_medida
FROM materias_primas mp
WHERE mp.id = ri.materia_prima_id;

ALTER TABLE receita_itens ALTER COLUMN unidade_medida SET NOT NULL;
