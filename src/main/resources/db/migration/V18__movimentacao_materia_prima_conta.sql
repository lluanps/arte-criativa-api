-- =========================================================
-- Rastreia quando uma movimentacao de materia-prima (ENTRADA/COMPRA) nasceu do
-- registro de uma Conta a pagar, em vez de vir direto da tela de "Registrar
-- movimentacao" -- evita duplicidade de despesa quando a mesma compra e lancada
-- como conta a pagar E como entrada de materia-prima (a despesa so nasce quando a
-- conta e paga, nao na hora de registrar a entrada).
--
-- conta_id: preenchido quando a movimentacao veio de uma conta AVULSA.
-- grupo_parcelamento_id: preenchido quando veio de uma conta PARCELADA (o grupo
-- inteiro, nao uma parcela especifica). Nunca os dois preenchidos ao mesmo tempo.
-- =========================================================

ALTER TABLE movimentacoes_materia_prima
    ADD COLUMN conta_id BIGINT,
    ADD COLUMN grupo_parcelamento_id UUID;

CREATE INDEX idx_movimentacao_materia_prima_conta ON movimentacoes_materia_prima (conta_id) WHERE conta_id IS NOT NULL;
CREATE INDEX idx_movimentacao_materia_prima_grupo ON movimentacoes_materia_prima (grupo_parcelamento_id) WHERE grupo_parcelamento_id IS NOT NULL;
