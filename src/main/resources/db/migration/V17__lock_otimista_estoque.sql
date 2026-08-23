-- =========================================================
-- Lock otimista (@Version do JPA/Hibernate) em produtos e materias_primas, pra evitar
-- "lost update": hoje registrarMovimentacao/ProducaoService.registrar/
-- VendaService.registrar/excluir fazem leitura -> calculo em memoria -> save do
-- estoqueAtual sem nenhum lock, entao duas escritas concorrentes no mesmo registro
-- podem se sobrescrever silenciosamente. DEFAULT 0 preenche as linhas existentes sem
-- quebrar migracao contra dado ja em producao.
-- =========================================================

ALTER TABLE produtos ADD COLUMN versao BIGINT NOT NULL DEFAULT 0;
ALTER TABLE materias_primas ADD COLUMN versao BIGINT NOT NULL DEFAULT 0;
