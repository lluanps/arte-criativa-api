-- =========================================================
-- Rollout de multi-tenancy (empresa_id) do piloto (V21: usuarios, fornecedores,
-- contas) pras demais tabelas de negócio que ganharam @TenantId em
-- EntidadeComEmpresa. Mesmo padrão: coluna NOT NULL, tudo que já existe vai pra
-- Empresa #1 (Arte Criativa).
-- =========================================================

ALTER TABLE canais_venda ADD COLUMN empresa_id BIGINT REFERENCES empresas(id);
UPDATE canais_venda SET empresa_id = 1;
ALTER TABLE canais_venda ALTER COLUMN empresa_id SET NOT NULL;
CREATE INDEX idx_canais_venda_empresa_id ON canais_venda (empresa_id);

ALTER TABLE categorias ADD COLUMN empresa_id BIGINT REFERENCES empresas(id);
UPDATE categorias SET empresa_id = 1;
ALTER TABLE categorias ALTER COLUMN empresa_id SET NOT NULL;
CREATE INDEX idx_categorias_empresa_id ON categorias (empresa_id);

ALTER TABLE categorias_materia_prima ADD COLUMN empresa_id BIGINT REFERENCES empresas(id);
UPDATE categorias_materia_prima SET empresa_id = 1;
ALTER TABLE categorias_materia_prima ALTER COLUMN empresa_id SET NOT NULL;
CREATE INDEX idx_categorias_materia_prima_empresa_id ON categorias_materia_prima (empresa_id);

ALTER TABLE clientes ADD COLUMN empresa_id BIGINT REFERENCES empresas(id);
UPDATE clientes SET empresa_id = 1;
ALTER TABLE clientes ALTER COLUMN empresa_id SET NOT NULL;
CREATE INDEX idx_clientes_empresa_id ON clientes (empresa_id);

ALTER TABLE materias_primas ADD COLUMN empresa_id BIGINT REFERENCES empresas(id);
UPDATE materias_primas SET empresa_id = 1;
ALTER TABLE materias_primas ALTER COLUMN empresa_id SET NOT NULL;
CREATE INDEX idx_materias_primas_empresa_id ON materias_primas (empresa_id);

ALTER TABLE materias_primas_desejadas ADD COLUMN empresa_id BIGINT REFERENCES empresas(id);
UPDATE materias_primas_desejadas SET empresa_id = 1;
ALTER TABLE materias_primas_desejadas ALTER COLUMN empresa_id SET NOT NULL;
CREATE INDEX idx_materias_primas_desejadas_empresa_id ON materias_primas_desejadas (empresa_id);

ALTER TABLE movimentacoes_materia_prima ADD COLUMN empresa_id BIGINT REFERENCES empresas(id);
UPDATE movimentacoes_materia_prima SET empresa_id = 1;
ALTER TABLE movimentacoes_materia_prima ALTER COLUMN empresa_id SET NOT NULL;
CREATE INDEX idx_movimentacoes_materia_prima_empresa_id ON movimentacoes_materia_prima (empresa_id);

ALTER TABLE movimentacoes_produto ADD COLUMN empresa_id BIGINT REFERENCES empresas(id);
UPDATE movimentacoes_produto SET empresa_id = 1;
ALTER TABLE movimentacoes_produto ALTER COLUMN empresa_id SET NOT NULL;
CREATE INDEX idx_movimentacoes_produto_empresa_id ON movimentacoes_produto (empresa_id);

ALTER TABLE produtos ADD COLUMN empresa_id BIGINT REFERENCES empresas(id);
UPDATE produtos SET empresa_id = 1;
ALTER TABLE produtos ALTER COLUMN empresa_id SET NOT NULL;
CREATE INDEX idx_produtos_empresa_id ON produtos (empresa_id);

ALTER TABLE lancamentos_financeiros ADD COLUMN empresa_id BIGINT REFERENCES empresas(id);
UPDATE lancamentos_financeiros SET empresa_id = 1;
ALTER TABLE lancamentos_financeiros ALTER COLUMN empresa_id SET NOT NULL;
CREATE INDEX idx_lancamentos_financeiros_empresa_id ON lancamentos_financeiros (empresa_id);

ALTER TABLE ideias ADD COLUMN empresa_id BIGINT REFERENCES empresas(id);
UPDATE ideias SET empresa_id = 1;
ALTER TABLE ideias ALTER COLUMN empresa_id SET NOT NULL;
CREATE INDEX idx_ideias_empresa_id ON ideias (empresa_id);

ALTER TABLE producoes ADD COLUMN empresa_id BIGINT REFERENCES empresas(id);
UPDATE producoes SET empresa_id = 1;
ALTER TABLE producoes ALTER COLUMN empresa_id SET NOT NULL;
CREATE INDEX idx_producoes_empresa_id ON producoes (empresa_id);

ALTER TABLE receitas ADD COLUMN empresa_id BIGINT REFERENCES empresas(id);
UPDATE receitas SET empresa_id = 1;
ALTER TABLE receitas ALTER COLUMN empresa_id SET NOT NULL;
CREATE INDEX idx_receitas_empresa_id ON receitas (empresa_id);

ALTER TABLE tutoriais ADD COLUMN empresa_id BIGINT REFERENCES empresas(id);
UPDATE tutoriais SET empresa_id = 1;
ALTER TABLE tutoriais ALTER COLUMN empresa_id SET NOT NULL;
CREATE INDEX idx_tutoriais_empresa_id ON tutoriais (empresa_id);

ALTER TABLE vendas ADD COLUMN empresa_id BIGINT REFERENCES empresas(id);
UPDATE vendas SET empresa_id = 1;
ALTER TABLE vendas ALTER COLUMN empresa_id SET NOT NULL;
CREATE INDEX idx_vendas_empresa_id ON vendas (empresa_id);
