-- =========================================================
-- Arte Criativa - Schema inicial
-- Modulos: Estoque, Receitas/Producao, Vendas,
--          Financeiro, Tutoriais, Usuarios
-- =========================================================

CREATE TABLE usuarios (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    senha_hash VARCHAR(255) NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------- ESTOQUE ----------

CREATE TABLE produtos (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    descricao TEXT,
    categoria VARCHAR(100),
    preco_venda NUMERIC(12,2) NOT NULL DEFAULT 0,
    estoque_atual NUMERIC(12,3) NOT NULL DEFAULT 0,
    estoque_minimo NUMERIC(12,3) NOT NULL DEFAULT 0,
    foto_url VARCHAR(500),
    ativo BOOLEAN NOT NULL DEFAULT true,
    criado_em TIMESTAMP NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE materias_primas (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    unidade_medida VARCHAR(20) NOT NULL,
    custo_unitario NUMERIC(12,4) NOT NULL DEFAULT 0,
    estoque_atual NUMERIC(12,3) NOT NULL DEFAULT 0,
    estoque_minimo NUMERIC(12,3) NOT NULL DEFAULT 0,
    fornecedor VARCHAR(150),
    criado_em TIMESTAMP NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE movimentacoes_produto (
    id BIGSERIAL PRIMARY KEY,
    produto_id BIGINT NOT NULL REFERENCES produtos(id),
    tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('ENTRADA','SAIDA')),
    motivo VARCHAR(20) NOT NULL CHECK (motivo IN ('PRODUCAO','VENDA','AJUSTE','PERDA')),
    quantidade NUMERIC(12,3) NOT NULL CHECK (quantidade > 0),
    observacao VARCHAR(500),
    data_movimentacao TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE movimentacoes_materia_prima (
    id BIGSERIAL PRIMARY KEY,
    materia_prima_id BIGINT NOT NULL REFERENCES materias_primas(id),
    tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('ENTRADA','SAIDA')),
    motivo VARCHAR(20) NOT NULL CHECK (motivo IN ('COMPRA','PRODUCAO','AJUSTE','PERDA')),
    quantidade NUMERIC(12,3) NOT NULL CHECK (quantidade > 0),
    observacao VARCHAR(500),
    data_movimentacao TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_movimentacoes_produto_produto_id ON movimentacoes_produto(produto_id);
CREATE INDEX idx_movimentacoes_materia_prima_materia_id ON movimentacoes_materia_prima(materia_prima_id);

-- ---------- RECEITAS / PRODUCAO ----------

CREATE TABLE receitas (
    id BIGSERIAL PRIMARY KEY,
    produto_id BIGINT NOT NULL UNIQUE REFERENCES produtos(id),
    nome VARCHAR(150) NOT NULL,
    rendimento NUMERIC(12,3) NOT NULL DEFAULT 1,
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE receita_itens (
    id BIGSERIAL PRIMARY KEY,
    receita_id BIGINT NOT NULL REFERENCES receitas(id) ON DELETE CASCADE,
    materia_prima_id BIGINT NOT NULL REFERENCES materias_primas(id),
    quantidade NUMERIC(12,3) NOT NULL CHECK (quantidade > 0)
);

CREATE TABLE producoes (
    id BIGSERIAL PRIMARY KEY,
    produto_id BIGINT NOT NULL REFERENCES produtos(id),
    quantidade_produzida NUMERIC(12,3) NOT NULL CHECK (quantidade_produzida > 0),
    custo_total NUMERIC(12,2) NOT NULL DEFAULT 0,
    observacao VARCHAR(500),
    data_producao TIMESTAMP NOT NULL DEFAULT now()
);

-- ---------- VENDAS ----------

CREATE TABLE vendas (
    id BIGSERIAL PRIMARY KEY,
    cliente_nome VARCHAR(150),
    canal VARCHAR(50),
    valor_total NUMERIC(12,2) NOT NULL DEFAULT 0,
    data_venda TIMESTAMP NOT NULL DEFAULT now(),
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE venda_itens (
    id BIGSERIAL PRIMARY KEY,
    venda_id BIGINT NOT NULL REFERENCES vendas(id) ON DELETE CASCADE,
    produto_id BIGINT NOT NULL REFERENCES produtos(id),
    quantidade NUMERIC(12,3) NOT NULL CHECK (quantidade > 0),
    preco_unitario NUMERIC(12,2) NOT NULL
);

-- ---------- FINANCEIRO ----------

CREATE TABLE lancamentos_financeiros (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('RECEITA','DESPESA')),
    categoria VARCHAR(100) NOT NULL,
    valor NUMERIC(12,2) NOT NULL CHECK (valor > 0),
    descricao VARCHAR(500),
    origem VARCHAR(20) NOT NULL DEFAULT 'MANUAL' CHECK (origem IN ('VENDA','COMPRA','MANUAL')),
    origem_id BIGINT,
    data_lancamento DATE NOT NULL,
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE contas (
    id BIGSERIAL PRIMARY KEY,
    tipo VARCHAR(10) NOT NULL CHECK (tipo IN ('PAGAR','RECEBER')),
    descricao VARCHAR(255) NOT NULL,
    valor NUMERIC(12,2) NOT NULL CHECK (valor > 0),
    vencimento DATE NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE' CHECK (status IN ('PENDENTE','PAGO','ATRASADO')),
    pago_em TIMESTAMP,
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_lancamentos_financeiros_data ON lancamentos_financeiros(data_lancamento);
CREATE INDEX idx_contas_vencimento ON contas(vencimento);

-- ---------- TUTORIAIS ----------

CREATE TABLE tutoriais (
    id BIGSERIAL PRIMARY KEY,
    titulo VARCHAR(200) NOT NULL,
    categoria VARCHAR(100),
    produto_relacionado_id BIGINT REFERENCES produtos(id),
    criado_em TIMESTAMP NOT NULL DEFAULT now(),
    atualizado_em TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE tutorial_passos (
    id BIGSERIAL PRIMARY KEY,
    tutorial_id BIGINT NOT NULL REFERENCES tutoriais(id) ON DELETE CASCADE,
    ordem INT NOT NULL,
    titulo VARCHAR(200) NOT NULL,
    descricao TEXT,
    midia_url VARCHAR(500)
);
