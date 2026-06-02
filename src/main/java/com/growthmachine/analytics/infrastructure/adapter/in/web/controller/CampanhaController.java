package com.growthmachine.analytics.infrastructure.adapter.in.web.controller;

import com.growthmachine.analytics.domain.exception.ErrorResponse;
import com.growthmachine.analytics.domain.model.Campanha;
import com.growthmachine.analytics.domain.model.enums.CanalOrigem;
import com.growthmachine.analytics.application.service.CampanhaService;
import io.swagger.v3.oas.annotations.Operation;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/campanhas")
@RequiredArgsConstructor
@Tag(name = "Campanhas", description = "Endpoints para gestão de campanhas de tráfego")
public class CampanhaController {

    private final CampanhaService service;
    private final PagedResourcesAssembler<Campanha> assembler;

    @PostMapping
    @Operation(summary = "Criar nova Campanha", description = "Cadastra uma nova campanha no sistema. Este endpoint é idempotente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Campanha criada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Campanha.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<Campanha>> criar(@Valid @RequestBody Campanha campanha) {
        Campanha nova = service.salvar(campanha);
        return ResponseEntity.status(HttpStatus.CREATED).body(toEntityModel(nova));
    }

    @GetMapping
    @Operation(summary = "Listar todas as Campanhas", description = "Retorna uma lista paginada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PagedModel<EntityModel<Campanha>>> listar(@PageableDefault(size = 10) Pageable pageable) {
        Page<Campanha> pagina = service.listarTodos(pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, this::toEntityModel));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Campanha por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrada."),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Campanha não encontrada.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<Campanha>> buscar(@PathVariable Long id) {
        Campanha campanha = service.buscarPorId(id);
        return ResponseEntity.ok(toEntityModel(campanha));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Campanha", description = "Altera os dados de uma campanha existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Campanha atualizada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Campanha.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados enviados.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Campanha não encontrada para atualização.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<Campanha>> atualizar(@PathVariable Long id, @Valid @RequestBody Campanha atualizada) {
        Campanha salva = service.atualizar(id, atualizada);
        return ResponseEntity.ok(toEntityModel(salva));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir Campanha")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Excluída com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Campanha não encontrada.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/busca-por-canal")
    @Operation(summary = "Filtrar por Canal de Origem", description = "Busca campanhas que pertençam a um canal de origem específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PagedModel<EntityModel<Campanha>>> buscarPorCanal(
            @RequestParam CanalOrigem canal,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Campanha> pagina = service.buscarPorCanal(canal, pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, this::toEntityModel));
    }

    @GetMapping("/busca-por-nome")
    @Operation(summary = "Filtrar por Nome da Campanha", description = "Busca campanhas que contenham o termo especificado no nome.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PagedModel<EntityModel<Campanha>>> buscarPorNome(
            @RequestParam String nome,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Campanha> pagina = service.buscarPorNome(nome, pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, this::toEntityModel));
    }

    private EntityModel<Campanha> toEntityModel(Campanha campanha) {
        EntityModel<Campanha> model = EntityModel.of(campanha);
        model.add(linkTo(methodOn(CampanhaController.class).buscar(campanha.getId())).withSelfRel());
        model.add(linkTo(methodOn(CampanhaController.class).listar(Pageable.unpaged())).withRel("lista"));
        model.add(linkTo(methodOn(CampanhaController.class).atualizar(campanha.getId(), campanha)).withRel("update"));
        model.add(linkTo(methodOn(CampanhaController.class).deletar(campanha.getId())).withRel("delete"));
        return model;
    }
}