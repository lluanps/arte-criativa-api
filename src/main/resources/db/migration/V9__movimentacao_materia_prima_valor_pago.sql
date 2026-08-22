-- =========================================================
-- Entrada de estoque de materia-prima passa a aceitar "quanto paguei no total" --
-- o backend calcula o custo unitario dessa compra (valor_pago / quantidade) e atualiza
-- o custo_unitario da materia-prima por media ponderada com o que ja tinha em estoque.
-- Colunas opcionais (NULL quando a entrada nao informa valor pago, ex: producao/ajuste),
-- entao nao muda nada pra movimentacoes ja existentes.
-- =========================================================

ALTER TABLE movimentacoes_materia_prima
    ADD COLUMN valor_pago NUMERIC(12,2),
    ADD COLUMN custo_unitario_apurado NUMERIC(12,4);
