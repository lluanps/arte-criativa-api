-- =========================================================
-- Suporte a conta parcelada: nao existe uma entidade "parcelamento" a parte, cada
-- parcela continua sendo uma linha normal em `contas` (paga/edita/exclui sozinha,
-- exatamente como ja funcionava) - as 3 colunas abaixo so marcam que um grupo de
-- contas veio da mesma compra parcelada, pra exibir "parcela X/N".
-- =========================================================

ALTER TABLE contas
    ADD COLUMN grupo_parcelamento_id UUID,
    ADD COLUMN numero_parcela INTEGER,
    ADD COLUMN total_parcelas INTEGER;

CREATE INDEX idx_contas_grupo_parcelamento ON contas (grupo_parcelamento_id) WHERE grupo_parcelamento_id IS NOT NULL;
