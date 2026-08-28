-- =========================================================
-- Prepara o terreno pra cadastro self-service de empresa nova (POST
-- /api/auth/registrar-empresa) sem fechar a porta pra régua de plano/cobrança que
-- ainda não foi desenhada: `ativa` é a "trava geral" mais simples possível --
-- suspender uma empresa (ex: inadimplência, futuro) sem precisar de nenhuma
-- tabela/coluna nova, só virar FALSE. Todo mundo que já existe nasce ativa.
-- =========================================================

ALTER TABLE empresas ADD COLUMN ativa BOOLEAN NOT NULL DEFAULT TRUE;
