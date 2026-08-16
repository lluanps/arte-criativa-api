# Arte Criativa — API

Backend do sistema de gestão para venda de produtos artesanais (velas, xícaras, etc).

Este repositório cobre só a API. O front fica em [arte-criativa-web](https://github.com/lluanps/arte-criativa-web).

## Stack

| Camada | Escolha |
|---|---|
| API | Java 17 + Spring Boot 3 (Maven) |
| Persistência | Spring Data JPA + Flyway (migrations versionadas) |
| Banco | PostgreSQL (local via Docker em dev, gerenciado na nuvem em produção) |
| Deploy | Railway ou Render (suporta Docker/Java de graça) |

## Módulos

1. **Estoque** — produtos finais e matérias-primas, com movimentações de entrada/saída
2. **Receitas / Produção** — ficha técnica por produto (consumo de matéria-prima), registro de produção com baixa automática e cálculo de custo
3. **Vendas** — pedidos que dão baixa no estoque de produto e geram lançamento financeiro
4. **Financeiro** — lançamentos (receita/despesa), contas a pagar/receber, dashboard de fluxo de caixa
5. **Tutoriais** — conteúdo passo a passo (texto + mídia), pode ligar a um produto/receita

## Modelo de dados

Ver `src/main/resources/db/migration/V1__schema_inicial.sql` — schema completo já criado com todas as tabelas dos 5 módulos + usuários.

## Roadmap

- [x] **Fase 0** — Setup: estrutura Maven da API, schema inicial do banco
- [x] **Fase 1** — Módulo Estoque: CRUD de produtos/matérias-primas + movimentações
- [x] **Fase 2** — Módulo Receitas/Produção (liga com Estoque) — testado ponta a ponta
- [x] **Fase 3** — Módulo Vendas (liga com Estoque e Financeiro)
- [x] **Fase 4** — Módulo Financeiro + dashboard
- [x] **Fase 5** — Autenticação (login) + Módulo Tutoriais

## Como rodar localmente

```bash
# 1. Sobe o Postgres local (porta 5433 no host — evita conflito com um
#    Postgres nativo que porventura já esteja instalado na máquina)
docker compose up -d

# 2. Roda a API (porta 8080)
mvn spring-boot:run
```

A API usa as variáveis `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` (com defaults apontando pro Postgres do `docker-compose.yml`, na porta 5433). Pra produção, aponte essas variáveis pro Postgres gerenciado (Supabase, Neon, Railway etc.).

Também usa `JWT_SECRET` (tem um default só pra dev — **troque em produção**, precisa de 32+ caracteres) e `JWT_EXPIRATION_MINUTES` (default 1440).

## Endpoints

- `/api/produtos`, `/api/produtos/{id}/movimentacoes`
- `/api/materias-primas`, `/api/materias-primas/{id}/movimentacoes`
- `/api/receitas`, `/api/receitas/produto/{produtoId}`
- `/api/producoes`, `/api/producoes/produto/{produtoId}`
- `/api/vendas`
- `/api/lancamentos-financeiros` (filtro opcional `?inicio=&fim=`)
- `/api/contas` (filtro opcional `?tipo=PAGAR|RECEBER`), `/api/contas/{id}/pagar`
- `/api/financeiro/dashboard` (opcional `?inicio=&fim=`, default mês corrente)
- `/api/auth/register`, `/api/auth/login` (retornam JWT)
- `/api/tutoriais`, `/api/tutoriais/produto/{produtoId}`

## Autenticação

Infra de login pronta (registro aberto + login, ambos emitindo JWT válido por
`JWT_EXPIRATION_MINUTES` — default 1440min/24h) e `JwtAuthFilter` já populando o
contexto de segurança quando um token válido é enviado. **Por decisão de escopo, os
endpoints de negócio (produtos, vendas, financeiro etc.) continuam públicos por
enquanto** — proteger rota por rota fica pra depois. Pra ligar a proteção geral, troca
`anyRequest().permitAll()` por `anyRequest().authenticated()` (mantendo
`/api/auth/**` liberado) em `SecurityConfig`.
