-- =========================================================
-- Contas marcadas como pagas ANTES da V11/ContaService.sincronizarLancamento existir
-- nunca geraram lancamento financeiro -- o dinheiro saiu/entrou de verdade, mas o
-- Financeiro nunca soube. Esse backfill cria, uma vez so, o lancamento que faltou pra
-- cada conta ja paga que ainda nao tem um (usa pago_em como data_lancamento, pra cair
-- no mes certo no dashboard -- nao "hoje").
--
-- So preenche o que nao existe ainda (NOT EXISTS), entao rodar de novo nao duplica --
-- e uma conta paga DEPOIS desta migration ja passa pelo fluxo normal do ContaService,
-- que ja cria o lancamento na hora.
-- =========================================================

INSERT INTO lancamentos_financeiros (tipo, categoria, valor, descricao, origem, origem_id, data_lancamento, criado_em)
SELECT
    CASE WHEN c.tipo = 'PAGAR' THEN 'DESPESA' ELSE 'RECEITA' END,
    CASE WHEN c.tipo = 'PAGAR' THEN 'Conta a pagar' ELSE 'Conta a receber' END,
    c.valor,
    c.descricao,
    'CONTA',
    c.id,
    COALESCE(c.pago_em::date, c.vencimento),
    COALESCE(c.pago_em, now())
FROM contas c
WHERE c.status = 'PAGO'
  AND NOT EXISTS (
      SELECT 1 FROM lancamentos_financeiros l WHERE l.origem = 'CONTA' AND l.origem_id = c.id
  );
