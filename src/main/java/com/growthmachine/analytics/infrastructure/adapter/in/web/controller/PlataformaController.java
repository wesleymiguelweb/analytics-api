package com.growthmachine.analytics.infrastructure.adapter.in.web.controller;

import com.growthmachine.analytics.domain.exception.ErrorResponse;
import com.growthmachine.analytics.domain.model.Plataforma;
import com.growthmachine.analytics.application.service.PlataformaService;
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
@RequestMapping("/api/plataformas")
@RequiredArgsConstructor
@Tag(name = "Plataformas", description = "Endpoints para o gerenciamento completo de Plataformas de Anúncio")
public class PlataformaController {

    private final PlataformaService service;
    private final PagedResourcesAssembler<Plataforma> assembler;

    @PostMapping
    @Operation(summary = "Criar Nova Plataforma", description = "Cadastra uma nova plataforma no sistema. Requer um nome único. Este endpoint é idempotente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Plataforma criada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Plataforma.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados enviados.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<Plataforma>> criar(@Valid @RequestBody Plataforma plataforma) {
        Plataforma nova = service.criar(plataforma);
        return ResponseEntity.status(HttpStatus.CREATED).body(toEntityModel(nova));
    }

    @GetMapping(headers = "X-API-Version=1")
    @Operation(summary = "Listar todas as plataformas (v1)", description = "Retorna uma lista paginada de plataformas.")
    public ResponseEntity<PagedModel<EntityModel<Plataforma>>> listar(@PageableDefault(size = 10) Pageable pageable) {
        Page<Plataforma> pagina = service.listar(pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, this::toEntityModel));
    }

    @GetMapping(headers = "X-API-Version=2")
    @Operation(summary = "Listar todas as plataformas (v2)", description = "Retorna uma lista simplificada de nomes de plataformas.")
    public Page<String> listarV2(@PageableDefault(size = 10) Pageable pageable) {
        return service.listar(pageable).map(Plataforma::getNome);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Plataforma por ID", description = "Recupera os detalhes de uma plataforma específica pelo seu ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plataforma encontrada.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Plataforma.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Plataforma não encontrada.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<Plataforma>> buscar(@PathVariable Long id) {
        Plataforma plataforma = service.buscar(id);
        return ResponseEntity.ok(toEntityModel(plataforma));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Plataforma", description = "Altera os dados de uma plataforma existente, como o seu nome.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plataforma atualizada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Plataforma.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Plataforma não encontrada.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<Plataforma>> atualizar(@PathVariable Long id, @Valid @RequestBody Plataforma plataforma) {
        Plataforma atualizada = service.atualizar(id, plataforma);
        return ResponseEntity.ok(toEntityModel(atualizada));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir Plataforma", description = "Remove permanentemente uma plataforma do sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Plataforma excluída com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autorizado.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Plataforma não encontrada.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/busca-por-nome")
    @Operation(summary = "Filtrar por Nome da Plataforma", description = "Busca plataformas que contenham o termo informado no nome.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autorizado.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PagedModel<EntityModel<Plataforma>>> buscarPorNome(
            @RequestParam String nome,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Plataforma> pagina = service.buscarPorNome(nome, pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, this::toEntityModel));
    }

    private EntityModel<Plataforma> toEntityModel(Plataforma plataforma) {
        EntityModel<Plataforma> model = EntityModel.of(plataforma);
        model.add(linkTo(methodOn(PlataformaController.class).buscar(plataforma.getId())).withSelfRel());
        model.add(linkTo(methodOn(PlataformaController.class).listar(Pageable.unpaged())).withRel("lista"));
        model.add(linkTo(methodOn(PlataformaController.class).atualizar(plataforma.getId(), plataforma)).withRel("update"));
        model.add(linkTo(methodOn(PlataformaController.class).deletar(plataforma.getId())).withRel("delete"));
        return model;
    }
}
