# criarCenarioTestesApiTests

Suite de testes de API (TestNG + RestAssured) para o projeto
[`criar-cenario-testes`](https://github.com/JeanHeberth/criar-cenario-testes).

## Stack

- Java 21, Gradle (wrapper incluso)
- TestNG 7.10.2
- RestAssured 5.5.0
- Allure (relatório + evidências de request/response)
- Lombok + Jackson (models de request)

## Estrutura

```
src/main/java
├── config/            # Environment (baseUrl via system property/env var) e Configuration (data.yaml)
├── constants/endpoints # Enum Endpoint — única fonte de verdade das rotas da API
├── clients/            # Wrappers RestAssured por domínio (cenario, jira, agent, autoqa)
├── models/request/     # DTOs de request espelhando os records da API
└── factories/          # Geração de payloads válidos/inválidos para os testes

src/test/java
├── base/BaseTest       # baseURI + filtros Allure/log
├── workflow/            # GET /cenario/workflows — smoke
├── cenario/             # /cenario — smoke + e2e (geração via IA)
├── jira/                # /jira/tasks/** — smoke (validação de path) + e2e
├── agent/               # /api/agents/** — smoke + e2e
└── autoqa/              # /api/auto-qa/executions/** — smoke + e2e
```

## Estratégia de suites: `smoke` vs `e2e`

A API depende de MongoDB, OpenAI/Gemini e Jira. Rodar geração de cenários
via IA (ou chamadas reais ao Jira) a cada merge seria lento, caro e
não-determinístico — o oposto do que se quer em um gate de CI. Por isso os
testes são divididos em dois grupos TestNG:

- **`smoke`**: contrato de API determinístico, sem custo de IA — validação
  de payload (400), recursos inexistentes (404), listagens e o endpoint
  `GET /cenario/workflows` (que não toca Mongo/IA/Jira). É o que roda a
  cada merge para `develop` (task Gradle `testSmoke`).
- **`e2e`**: fluxos completos que geram conteúdo real via IA ou chamam o
  Jira de verdade (task Gradle `testRegression`, inclui `smoke` + `e2e`).
  Rodar sob demanda ou em pipeline agendado, não a cada commit.

Alguns testes `e2e` estão com `enabled = false` e lançam `SkipException`
propositalmente — dependem de DTOs cujo shape completo (`AgentChatRequest`,
`AutoQaCreateExecutionRequest#scenario`) não é público nos controllers
lidos; preencha os campos reais antes de habilitá-los.

### Observações de contrato encontradas durante a escrita dos testes

- `GET /cenario/{id}` com id inexistente retorna **200 com corpo vazio**
  (não 404) — `CenarioService#buscarCenario` faz `findById(id).orElse(null)`.
  Documentado em `CenarioTest`, não corrigido silenciosamente.
- `POST /api/agents/chat` usa `@Valid`, mas `ApiExceptionHandler` (genérico)
  não tem handler dedicado para `MethodArgumentNotValidException` — ao
  contrário de `AutoQaExecutionController`, que tem
  `AutoQaExecutionExceptionHandler` garantindo 400 explicitamente. É
  possível que erros de validação no `AgentController` surjam como 500 em
  vez de 400. `AgentTest` e `JiraTest` (validação de `taskKey` via
  `@Validated` + `@Pattern`) por isso só afirmam "não é sucesso"
  (`statusCode >= 400`) em vez de fixar o código exato — vale o time
  confirmar/fechar esse contrato.

## Rodando localmente

```bash
# API precisa estar de pé em http://localhost:8089
# (MONGO_URI_NUVEM, OPENAI_API_KEY, GEMINI_API_KEY configurados no .env dela)

./gradlew testSmoke        # suite rápida
./gradlew testRegression   # suite completa (inclui e2e)
./gradlew testWorkflow     # só um domínio, ex.: workflow/cenario/jira/agent/autoqa
```

Apontar para outra URL de API:

```bash
./gradlew testSmoke -Dapi.baseUrl=http://100.83.72.100:9999
```

Relatório Allure:

```bash
./gradlew allureReport
open build/allure-report/index.html
```

## Jenkins

O [`Jenkinsfile`](./Jenkinsfile) deste repositório:

1. Faz checkout deste projeto de testes e, à parte, do repositório da API
   (`API_GIT_URL`/`API_GIT_BRANCH`, default `develop`).
2. Sobe o backend da API via `docker compose` (usa o `docker-compose.yml`
   já existente na API), injetando segredos via Jenkins Credentials.
3. Aguarda `GET /cenario/workflows` responder (readiness probe — a API não
   expõe `/actuator/health`).
4. Roda `testSmoke` (ou `testRegression`, via parâmetro `TEST_SUITE`).
5. Publica JUnit + Allure e derruba o `docker compose` no `post always`.

### Credenciais Jenkins necessárias (tipo "Secret text")

**Obrigatórias** — sem elas a API nem sobe (`application.yml` não tem
default para essas três):

| Credential ID                        | Variável de ambiente na API |
|---------------------------------------|------------------------------|
| `criar-cenario-testes-mongo-uri`      | `MONGO_URI_NUVEM`            |
| `criar-cenario-testes-openai-key`     | `OPENAI_API_KEY`             |
| `criar-cenario-testes-gemini-key`     | `GEMINI_API_KEY`             |

**Opcionais** — têm default vazio em `application.yml`
(`${JIRA_BASE_URL:}`) e a suite `smoke` nunca chama o Jira de verdade
(só valida formato de `taskKey` antes de qualquer request externa). Se
não forem criadas, o pipeline segue normalmente — só são necessárias
para rodar o teste `e2e` de Jira (`testRegression`):

| Credential ID                        | Variável de ambiente na API |
|---------------------------------------|------------------------------|
| `criar-cenario-testes-jira-base-url`  | `JIRA_BASE_URL`              |
| `criar-cenario-testes-jira-email`     | `JIRA_EMAIL`                 |
| `criar-cenario-testes-jira-api-token` | `JIRA_API_TOKEN`             |

Como criar: **Gerenciar Jenkins → Credentials → System → Global
credentials (unrestricted) → Add Credentials → kind "Secret text"**,
usando exatamente os IDs acima.

### Disparo automático em merge para `develop` da API

Este Jenkinsfile mora no repo de testes, mas precisa reagir a pushes na
`develop` do repo da **API** (repositório diferente). Isso é configuração
do job, não do Pipeline script. Duas formas, veja o comentário no topo do
`Jenkinsfile` para o snippet completo:

1. **Feito** — o `Jenkinsfile` da API já tem o stage `Disparar Testes
   Automatizados (develop)`, que chama
   `build job: 'criarCenarioTesteAPITestes', wait: false` quando a branch
   for `develop`. Exige que o job neste repo se chame exatamente
   `criarCenarioTesteAPITestes` no Jenkins (é o nome atual).
2. Configurar este job no Jenkins com um webhook do GitHub/GitLab apontando
   para o repositório da API (branch `develop`) via plugin *Generic
   Webhook Trigger*, mantendo o "Pipeline script from SCM" apontando para
   este repositório de testes.

O parâmetro `API_GIT_BRANCH` permite rodar manualmente contra qualquer
branch da API antes mesmo do merge.
