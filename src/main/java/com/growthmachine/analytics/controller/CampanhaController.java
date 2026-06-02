package com.growthmachine.analytics.controller;

import com.growthmachine.analytics.model.Campanha;
import com.growthmachine.analytics.model.enums.CanalOrigem;
import com.growthmachine.analytics.service.CampanhaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/campanhas")
@RequiredArgsConstructor
@Tag(name = "Campanhas", description = "Endpoints para gestão de campanhas de tráfego")
public class CampanhaController {

    private final CampanhaService service;
    private final PagedResourcesAssembler<Campanha> assembler;

    @PostMapping
    @Operation(summary = "Criar nova Campanha", description = "Cadastra uma nova campanha no sistema. Este endpoint é idempotente.")
    @Parameter(in = ParameterIn.HEADER, name = "X-Idempotency-Key", description = "Chave para garantir que a operação não seja executada duas vezes.", required = false)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Campanha criada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Campanha.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<EntityModel<Campanha>> criar(@Valid @RequestBody Campanha campanha) {
        Campanha nova = service.salvar(campanha);
        EntityModel<Campanha> entityModel = EntityModel.of(nova);
        nova.addLinks(entityModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @GetMapping
    @Operation(summary = "Listar todas as Campanhas", description = "Retorna uma lista paginada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<PagedModel<EntityModel<Campanha>>> listar(@PageableDefault(size = 10) Pageable pageable) {
        Page<Campanha> pagina = service.listarTodos(pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, campanha -> {
            EntityModel<Campanha> entityModel = EntityModel.of(campanha);
            campanha.addLinks(entityModel);
            return entityModel;
        }));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Campanha por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrada."),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Campanha não encontrada."),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<EntityModel<Campanha>> buscar(@PathVariable Long id) {
        Campanha campanha = service.buscarPorId(id);
        EntityModel<Campanha> entityModel = EntityModel.of(campanha);
        campanha.addLinks(entityModel);
        return ResponseEntity.ok(entityModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Campanha", description = "Altera os dados de uma campanha existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Campanha atualizada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Campanha.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados enviados.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Campanha não encontrada para atualização.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<EntityModel<Campanha>> atualizar(@PathVariable Long id, @Valid @RequestBody Campanha atualizada) {
        Campanha salva = service.atualizar(id, atualizada);
        EntityModel<Campanha> entityModel = EntityModel.of(salva);
        salva.addLinks(entityModel);
        return ResponseEntity.ok(entityModel);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir Campanha")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Excluída com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Campanha não encontrada."),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/busca-por-canal")
    @Operation(summary = "Filtrar por Canal de Origem", description = "Busca campanhas que pertençam a um canal de origem específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<PagedModel<EntityModel<Campanha>>> buscarPorCanal(
            @RequestParam CanalOrigem canal,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Campanha> pagina = service.buscarPorCanal(canal, pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, campanha -> {
            EntityModel<Campanha> entityModel = EntityModel.of(campanha);
            campanha.addLinks(entityModel);
            return entityModel;
        }));
    }
}