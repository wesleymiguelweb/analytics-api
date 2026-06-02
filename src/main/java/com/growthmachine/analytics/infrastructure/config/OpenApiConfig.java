package com.growthmachine.analytics.infrastructure.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.examples.Example;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Configuration
public class OpenApiConfig {

    @Value("${analytics.api.fixed-key}")
    private String fixedKey;

    @Bean
    public OpenAPI customOpenAPI() {
        String description = """
                API para gestão de marketing de crescimento.

                Como usar:
                1. Para testes, use a chave fixa %s no header X-API-Key.
                2. Se preferir, gere uma chave dinamica em POST /api/keys?owner=seu-nome.
                3. Clique em Authorize no Swagger UI e informe a chave no header X-API-Key.
                4. Em operações POST, envie X-Idempotency-Key quando quiser evitar processamento duplicado.
                5. Listagens aceitam paginação por page, size e sort.
                6. Endpoints versionados usam o header X-API-Version. Exemplo: GET /api/plataformas com X-API-Version=1 ou X-API-Version=2.
                7. Para uso prático de Growth, registre métricas em /api/metricas e consulte /api/analytics/campanhas/{campanhaId}/indicadores para transformar dados mensurados em KPIs, diagnóstico e sugestões.

                Códigos de status documentados:
                200 OK para consultas e atualizações bem-sucedidas; 201 Created para criação; 204 No Content para exclusão; 400 Bad Request para validação, corpo inválido ou parâmetro inválido; 401 Unauthorized para X-API-Key ausente ou inválida; 404 Not Found para recurso inexistente; 409 Conflict para conflitos de integridade; 429 Too Many Requests para rate limit excedido; 500 Internal Server Error para erro inesperado.
                """.formatted(fixedKey);

        return new OpenAPI()
                .info(new Info()
                        .title("Analytics API")
                        .version("2.0.0")
                        .description(description)
                        .contact(new Contact()
                                .name("Wesley Miguel")
                                .url("https://github.com/wesleymiguelweb")))
                .components(new Components()
                        .addSecuritySchemes("apiKey",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.HEADER)
                                        .name("X-API-Key")
                                        .description("Use a chave fixa " + fixedKey + " ou uma chave gerada por POST /api/keys?owner=seu-nome.")));
    }

    @Bean
    public OperationCustomizer operationCustomizer() {
        return (operation, handlerMethod) -> {
            String controllerName = handlerMethod.getBeanType().getSimpleName();
            String methodName = handlerMethod.getMethod().getName();
            boolean isCreateApiKey = "ApiKeyController".equals(controllerName) && "createKey".equals(methodName);

            if (!isCreateApiKey) {
                operation.addSecurityItem(new SecurityRequirement().addList("apiKey"));
                addHeader(operation, "X-API-Key", "Chave obrigatória para endpoints protegidos. Use a chave fixa " + fixedKey + " ou gere uma chave em POST /api/keys?owner=seu-nome.", true, fixedKey);
                addResponse(operation, "401", "Não autorizado. Header X-API-Key ausente, vazio ou inválido.", errorExample("Acesso negado: Chave de API (X-API-Key) inválida ou ausente.", 401));
            }

            if (handlerMethod.hasMethodAnnotation(PostMapping.class) && !isCreateApiKey) {
                addHeader(operation, "X-Idempotency-Key", "Chave opcional para tornar o POST idempotente. Ao reenviar a mesma chave para a mesma URI, a API reaproveita a resposta salva.", false, "pedido-123-criacao");
            }

            if ("PlataformaController".equals(controllerName) && handlerMethod.hasMethodAnnotation(GetMapping.class)
                    && ("listar".equals(methodName) || "listarV2".equals(methodName))) {
                addHeader(operation, "X-API-Version", "Versão do endpoint. Use 1 para resposta HATEOAS paginada ou 2 para lista simplificada de nomes.", true, "1");
            }

            if ("SugestaoController".equals(controllerName) && handlerMethod.getBeanType().getPackageName().endsWith(".v1")) {
                addHeader(operation, "X-API-Version", "Versão obrigatória para este endpoint demonstrativo.", true, "1");
            }

            if ("SugestaoController".equals(controllerName) && handlerMethod.getBeanType().getPackageName().endsWith(".v2")) {
                addHeader(operation, "X-API-Version", "Versão obrigatória para este endpoint demonstrativo.", true, "2");
            }

            addResponse(operation, "400", "Requisição inválida: erro de validação, parâmetro incompatível, enum/data inválidos ou corpo JSON mal formatado.", errorExample("Erro de validação", 400));
            addResponse(operation, "409", "Conflito de dados: violação de campo único ou relacionamento inválido.", errorExample("Conflito de dados: verifique campos únicos e relacionamentos informados.", 409));
            addResponse(operation, "429", "Limite de requisições excedido. A resposta inclui Retry-After e X-RateLimit-*.", errorExample("Limite de requisições excedido. Tente novamente mais tarde.", 429));
            addResponse(operation, "500", "Erro inesperado no servidor.", errorExample("Ocorreu um erro inesperado no servidor. Por favor, tente novamente mais tarde.", 500));

            return operation;
        };
    }

    private void addHeader(Operation operation, String name, String description, boolean required, String example) {
        if (operation.getParameters() != null) {
            for (Parameter parameter : operation.getParameters()) {
                if (name.equals(parameter.getName())) {
                    parameter.setSchema(parameter.getSchema() != null ? parameter.getSchema() : new StringSchema());
                    parameter.setDescription(parameter.getDescription() != null ? parameter.getDescription() : description);
                    parameter.setRequired(parameter.getRequired() != null ? parameter.getRequired() : required);
                    parameter.setExample(parameter.getExample() != null ? parameter.getExample() : example);
                    return;
                }
            }
        }

        operation.addParametersItem(new Parameter()
                .in("header")
                .name(name)
                .required(required)
                .description(description)
                .schema(new StringSchema())
                .example(example));
    }

    private void addResponse(Operation operation, String code, String description, Example example) {
        if (operation.getResponses() != null && operation.getResponses().containsKey(code)) {
            return;
        }

        operation.getResponses().addApiResponse(code, new ApiResponse()
                .description(description)
                .content(new Content().addMediaType("application/json", new MediaType().addExamples("exemplo", example))));
    }

    private Example errorExample(String message, int status) {
        return new Example().value("""
                {
                  "message": "%s",
                  "status": %d,
                  "timestamp": "2026-06-02T08:30:00",
                  "errors": null
                }
                """.formatted(message, status));
    }
}
