-- =========================================================
-- Multi-tenancy por coluna empresa_id (banco único, isolado por WHERE empresa_id = ?
-- via Hibernate @TenantId). Empresa #1 recebe todo o dado existente automaticamente.
--
-- Escopo desta migration: infraestrutura (tabela empresas) + as entidades PILOTO
-- (usuarios, fornecedores, contas) pra provar o mecanismo de ponta a ponta com risco
-- pequeno antes do rollout pras demais 12 tabelas de negócio (fica pra uma migration
-- V22 separada, depois que este piloto estiver validado).
-- =========================================================

CREATE TABLE empresas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150),
    telefone VARCHAR(30),
    cnpj_ou_cpf VARCHAR(20),
    endereco TEXT,
    logotipo_url VARCHAR(500),
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);

INSERT INTO empresas (id, nome, email) VALUES (1, 'Arte Criativa', 'lluanps@gmail.com');
-- garante que o próximo INSERT via GENERATED/serial não colida com o id 1 setado à mão
SELECT setval(pg_get_serial_sequence('empresas', 'id'), 1);

ALTER TABLE usuarios ADD COLUMN empresa_id BIGINT REFERENCES empresas(id);
UPDATE usuarios SET empresa_id = 1;
ALTER TABLE usuarios ALTER COLUMN empresa_id SET NOT NULL;
CREATE INDEX idx_usuarios_empresa_id ON usuarios (empresa_id);

ALTER TABLE fornecedores ADD COLUMN empresa_id BIGINT REFERENCES empresas(id);
UPDATE fornecedores SET empresa_id = 1;
ALTER TABLE fornecedores ALTER COLUMN empresa_id SET NOT NULL;
CREATE INDEX idx_fornecedores_empresa_id ON fornecedores (empresa_id);

ALTER TABLE contas ADD COLUMN empresa_id BIGINT REFERENCES empresas(id);
UPDATE contas SET empresa_id = 1;
ALTER TABLE contas ALTER COLUMN empresa_id SET NOT NULL;
CREATE INDEX idx_contas_empresa_id ON contas (empresa_id);
