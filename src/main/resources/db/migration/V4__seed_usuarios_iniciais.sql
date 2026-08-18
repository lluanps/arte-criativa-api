-- =========================================================
-- Bootstrap: primeiros dois usuários, já que /api/auth/register
-- passou a exigir login (ninguém conseguiria criar o primeiro
-- usuário sem isso). Senha inicial "admin" pros dois — trocar
-- pelo fluxo de "esqueci minha senha" assim que possível.
-- =========================================================

INSERT INTO usuarios (nome, email, senha_hash) VALUES
    ('Luan Pinheiro', 'lluanps@gmail.com', '$2a$10$utuhvbllVtMgHI7vHjPbduq7G0d7kocAXhptLcgP0CUo4OR1CU0jq'),
    ('Eduarda', 'eduarda.tvrs2108@gmail.com', '$2a$10$XH0fVInFb0uYhLWCLJBuNu1L9wX2GwxWPNFA.rgUyCQ8PghTzo/ci');
