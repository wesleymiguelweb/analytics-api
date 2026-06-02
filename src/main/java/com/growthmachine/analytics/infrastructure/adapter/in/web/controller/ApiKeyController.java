package com.growthmachine.analytics.infrastructure.adapter.in.web.controller;

import com.growthmachine.analytics.domain.model.ApiKey;
import com.growthmachine.analytics.application.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/keys")
@Validated
@Tag(name = "API Keys", description = "Endpoints para gerenciamento de chaves de API")
public class ApiKeyController {

    @Autowired
    private ApiKeyService apiKeyService;

    @PostMapping
    @Operation(
            summary = "Gerar nova chave de API",
            description = "Cria uma chave de API para um proprietário. Este é o endpoint inicial de uso da API e não exige X-API-Key. Depois de gerar a chave, use o valor retornado no header X-API-Key dos demais endpoints."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Chave criada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApiKey.class), examples = @ExampleObject(value = "{\"apiKey\":\"9f1f7e61-4d2a-4d8e-9a6b-0c1d2e3f4a5b\",\"owner\":\"wesley\"}"))),
            @ApiResponse(responseCode = "400", description = "Owner ausente ou inválido."),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido."),
            @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor.")
    })
    public ResponseEntity<ApiKey> createKey(
            @Parameter(description = "Identificação do proprietário da chave.", example = "wesley", required = true)
            @RequestParam @NotBlank(message = "O proprietário da chave é obrigatório") String owner) {
        return ResponseEntity.status(HttpStatus.CREATED).body(apiKeyService.createKey(owner));
    }

    @DeleteMapping
    @Operation(
            summary = "Deletar chave de API",
            description = "Revoga uma chave de API existente. Requer autenticação via X-API-Key válida."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Chave excluída com sucesso."),
            @ApiResponse(responseCode = "400", description = "Parâmetro key ausente ou inválido."),
            @ApiResponse(responseCode = "401", description = "X-API-Key ausente ou inválida."),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido."),
            @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor.")
    })
    public ResponseEntity<Void> deleteKey(
            @Parameter(description = "Chave que será revogada.", example = "9f1f7e61-4d2a-4d8e-9a6b-0c1d2e3f4a5b", required = true)
            @RequestParam @NotBlank(message = "A chave de API é obrigatória") String key) {
        apiKeyService.deleteKey(key);
        return ResponseEntity.noContent().build();
    }
}
