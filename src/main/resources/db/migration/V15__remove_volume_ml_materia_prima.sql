-- =========================================================
-- Campo "Volume (ml)" em matéria-prima nunca teve uso real: não entra em nenhum
-- cálculo (custo, ficha técnica, estoque baixo) nem é exibido na listagem — só
-- ocupava espaço nos formulários de criar/editar. Removido a pedido do usuário.
-- Continua existindo em Produto (lá sim é usado: exibido na listagem e no
-- compartilhamento via IA), essa migration só mexe em materias_primas.
-- =========================================================

ALTER TABLE materias_primas DROP COLUMN volume_ml;
