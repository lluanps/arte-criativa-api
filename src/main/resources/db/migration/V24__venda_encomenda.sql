-- Encomendas com status e prazo: venda ganha data de entrega combinada (opcional),
-- status do pedido e sinal/entrada. Vendas existentes (balcão) viram status ENTREGUE
-- por default -- correto, já foram entregues.
ALTER TABLE vendas ADD COLUMN data_entrega_prevista DATE;
ALTER TABLE vendas ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ENTREGUE';
ALTER TABLE vendas ADD COLUMN valor_sinal NUMERIC(12, 2) NOT NULL DEFAULT 0;
