-- =========================================================
-- Custos extras de uma compra vinculada a Conta (ver V18) que nao sao de nenhuma
-- materia-prima especifica -- ex: frete, taxas. Nao gera movimentacao de estoque
-- nenhuma, so entra na soma que precisa bater com o valor da conta:
-- soma(itens.valor) + custosExtras == valor (ou valorTotal, na parcelada).
--
-- NOT NULL DEFAULT 0 (em vez de nullable) pra seguir o mesmo padrao ja usado nos
-- demais campos monetarios/quantidade da base (ex: Produto.estoqueAtual) -- uma conta
-- sem custos extras tem 0, nao null, evitando null-check espalhado pelo codigo.
-- =========================================================

ALTER TABLE contas ADD COLUMN custos_extras NUMERIC(12, 2) NOT NULL DEFAULT 0;
