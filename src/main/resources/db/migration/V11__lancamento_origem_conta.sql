-- =========================================================
-- OrigemLancamento ganha o valor CONTA (conta a pagar/receber marcada como paga passa
-- a gerar um lancamento de verdade -- ver ContaService.sincronizarLancamento). O CHECK
-- constraint original (V1) so permitia VENDA/COMPRA/MANUAL; precisa incluir CONTA.
-- =========================================================

ALTER TABLE lancamentos_financeiros DROP CONSTRAINT lancamentos_financeiros_origem_check;

ALTER TABLE lancamentos_financeiros
    ADD CONSTRAINT lancamentos_financeiros_origem_check CHECK (origem IN ('VENDA', 'COMPRA', 'CONTA', 'MANUAL'));
