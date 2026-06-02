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

Crie a primeira chave de API:

```http
POST /api/keys?owner=wesley
```

Use a chave retornada nos demais endpoints:

```http
X-API-Key: chave-gerada
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
- Rate limiting com headers `X-RateLimit-*` e `Retry-After`
- CORS para origens especificas
- Tratamento global de erros com `@ControllerAdvice`
