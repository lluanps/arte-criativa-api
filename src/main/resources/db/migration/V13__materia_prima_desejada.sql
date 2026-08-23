-- =========================================================
-- Lista de compras: matéria-prima "só o nome, ainda sem preço" -- tabela totalmente
-- separada de materias_primas, pra nunca vazar pro seletor de ficha técnica, alerta
-- de estoque baixo ou qualquer busca de matéria-prima "de verdade" antes da compra
-- ser registrada. Ver MateriaPrimaDesejada.java e MateriaPrimaService.criar.
-- =========================================================

CREATE TABLE materias_primas_desejadas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);
