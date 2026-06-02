# Analytics API

API Spring Boot para gestao de contas anunciantes, campanhas, plataformas, metricas diarias, metas estrategicas e sugestoes de otimizacao.

## Requisitos

- Java 17 ou superior. Este projeto foi validado com JDK 25.
- IntelliJ IDEA
- Maven Wrapper incluso no projeto

## Como rodar no IntelliJ

1. Abra a pasta do projeto pelo arquivo `pom.xml`.
2. Em `File > Project Structure`, selecione um SDK Java 17 ou superior.
3. Aguarde o IntelliJ importar as dependencias Maven.
4. Rode a classe `com.growthmachine.analytics.AnalyticsApiApplication`.

Na maquina local, o JDK 25 foi encontrado em:

```text
C:\Users\wesle\.jdks\openjdk-25.0.2
```

Se o terminal nao reconhecer `java`, configure `JAVA_HOME` para esse caminho ou selecione esse SDK no IntelliJ. O `pom.xml` usa `java.version=17` para gerar bytecode compativel, mas pode ser compilado e executado com o JDK 25 normalmente.

Tambem e possivel rodar pelo terminal:

```bash
./mvnw spring-boot:run
```

No Windows:

```bat
mvnw.cmd spring-boot:run
```

## URLs principais

- Aplicacao: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`
- H2 Console: `http://localhost:8080/h2-console`

Dados do H2:

- JDBC URL: `jdbc:h2:mem:growthdb`
- User: `sa`
- Password: vazio

## Autenticacao

Para facilitar testes e avaliacao, a API aceita uma chave fixa:

```http
X-API-Key: analytics-dev-key-2026
```

Tambem e possivel criar uma chave dinamica:

```http
POST /api/keys?owner=wesley
```

Use a chave retornada ou a chave fixa nos demais endpoints:

```http
X-API-Key: analytics-dev-key-2026
```

Operacoes `POST` aceitam idempotencia:

```http
X-Idempotency-Key: qualquer-chave-unica
```

## Versionamento

Exemplo de endpoints versionados por header:

```http
GET /api/plataformas
X-API-Version: 1
```

```http
GET /api/plataformas
X-API-Version: 2
```

## Uso pratico de Growth Analytics

Ao iniciar com H2, a API cria uma massa demo quando o banco esta vazio:

- Conta: `Growth Machine Demo`
- Campanha: `Demo - Performance Ecommerce`
- Periodo com metricas: `2026-03-01` a `2026-03-05`

O fluxo principal de mercado e:

1. Criar conta anunciante, meta estrategica, campanha e plataformas.
2. Registrar metricas mensuradas de campanha em `POST /api/metricas`.
3. Consultar indicadores consolidados:

```http
GET /api/analytics/campanhas/1/indicadores?dataInicio=2026-03-01&dataFim=2026-03-31
X-API-Key: analytics-dev-key-2026
```

4. Simular uma recomendacao sem salvar:

```http
GET /api/analytics/campanhas/1/sugestao?dataInicio=2026-03-01&dataFim=2026-03-31
X-API-Key: analytics-dev-key-2026
```

5. Gerar e salvar a sugestao no historico:

```http
POST /api/analytics/campanhas/1/sugestoes?dataInicio=2026-03-01&dataFim=2026-03-31
X-API-Key: analytics-dev-key-2026
X-Idempotency-Key: sugestao-campanha-1-marco-2026
```

Os indicadores calculados incluem CTR, taxa de conversao, CPC, CPA, ticket medio, ROAS, ROAS alvo, margem contra a meta, diagnostico executivo e alertas de decisao.

## Recursos implementados

- Spring Boot 4.0.6, Java 17, Maven, H2 e Spring Data JPA
- Entidades com relacionamentos `One-to-One`, `One-to-Many` e `Many-to-Many`
- Bean Validation nas entidades
- CRUD completo com listagens paginadas
- Consultas personalizadas por entidade
- Swagger/OpenAPI com Springdoc
- HATEOAS com `EntityModel` e `PagedModel`
- API Key via `X-API-Key`
- Idempotencia via `X-Idempotency-Key`
- Camada analitica para transformar metricas mensuradas em indicadores e sugestoes acionaveis
- Rate limiting com headers `X-RateLimit-*` e `Retry-After`
- CORS para origens especificas
- Tratamento global de erros com `@ControllerAdvice`
