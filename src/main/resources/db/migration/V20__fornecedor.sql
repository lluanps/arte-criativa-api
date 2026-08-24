-- =========================================================
-- Fornecedor vira cadastro de verdade (antes era texto livre na coluna
-- materias_primas.fornecedor) -- permite consultar "o que compro de quem" e corrigir
-- o nome num lugar só sem quebrar o vínculo com o histórico.
-- =========================================================

CREATE TABLE fornecedores (
    id BIGSERIAL PRIMARY KEY,
    nome VARCHAR(150) NOT NULL,
    telefone VARCHAR(30),
    observacao TEXT,
    criado_em TIMESTAMP NOT NULL DEFAULT now()
);

-- Migra cada texto distinto já digitado pra um fornecedor real (ignora vazio/nulo).
INSERT INTO fornecedores (nome)
SELECT DISTINCT trim(fornecedor)
FROM materias_primas
WHERE fornecedor IS NOT NULL AND trim(fornecedor) <> '';

ALTER TABLE materias_primas ADD COLUMN fornecedor_id BIGINT REFERENCES fornecedores(id);

UPDATE materias_primas mp
SET fornecedor_id = f.id
FROM fornecedores f
WHERE trim(mp.fornecedor) = f.nome;

ALTER TABLE materias_primas DROP COLUMN fornecedor;
