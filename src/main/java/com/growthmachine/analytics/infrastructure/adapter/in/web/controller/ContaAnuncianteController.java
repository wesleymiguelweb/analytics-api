package com.growthmachine.analytics.infrastructure.adapter.in.web.controller;

import com.growthmachine.analytics.domain.exception.ErrorResponse;
import com.growthmachine.analytics.domain.model.ContaAnunciante;
import com.growthmachine.analytics.application.service.ContaAnuncianteService;
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
@RequestMapping("/api/contas")
@RequiredArgsConstructor
@Tag(name = "Contas Anunciantes", description = "Endpoints para gestão das empresas (Anunciantes) no sistema")
public class ContaAnuncianteController {

    private final ContaAnuncianteService service;
    private final PagedResourcesAssembler<ContaAnunciante> assembler;

    @PostMapping
    @Operation(summary = "Criar nova Conta Anunciante", description = "Cadastra uma nova empresa no sistema. Este endpoint é idempotente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta criada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ContaAnunciante.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados enviados.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<ContaAnunciante>> criar(@Valid @RequestBody ContaAnunciante conta) {
        ContaAnunciante nova = service.salvar(conta);
        return ResponseEntity.status(HttpStatus.CREATED).body(toEntityModel(nova));
    }

    @GetMapping
    @Operation(summary = "Listar todas as Contas", description = "Retorna uma lista paginada de todas as contas anunciantes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PagedModel<EntityModel<ContaAnunciante>>> listar(@PageableDefault(size = 10) Pageable pageable) {
        Page<ContaAnunciante> pagina = service.listarTodos(pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, this::toEntityModel));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Conta por ID", description = "Recupera os detalhes de uma conta específica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta encontrada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ContaAnunciante.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada no banco de dados.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<ContaAnunciante>> buscar(@PathVariable Long id) {
        ContaAnunciante conta = service.buscarPorId(id);
        return ResponseEntity.ok(toEntityModel(conta));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Conta", description = "Altera os dados de uma empresa existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta atualizada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ContaAnunciante.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados enviados.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada para atualização.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<ContaAnunciante>> atualizar(@PathVariable Long id, @Valid @RequestBody ContaAnunciante atualizada) {
        ContaAnunciante salva = service.atualizar(id, atualizada);
        return ResponseEntity.ok(toEntityModel(salva));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir Conta", description = "Remove permanentemente uma conta do sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Conta excluída com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/busca-por-nome")
    @Operation(summary = "Filtrar por Nome da Empresa", description = "Busca contas anunciantes que contenham o termo especificado no nome.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PagedModel<EntityModel<ContaAnunciante>>> buscarPorNome(
            @RequestParam String nome,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ContaAnunciante> pagina = service.buscarPorNome(nome, pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, this::toEntityModel));
    }

    private EntityModel<ContaAnunciante> toEntityModel(ContaAnunciante conta) {
        EntityModel<ContaAnunciante> model = EntityModel.of(conta);
        model.add(linkTo(methodOn(ContaAnuncianteController.class).buscar(conta.getId())).withSelfRel());
        model.add(linkTo(methodOn(ContaAnuncianteController.class).listar(Pageable.unpaged())).withRel("lista"));
        model.add(linkTo(methodOn(ContaAnuncianteController.class).atualizar(conta.getId(), conta)).withRel("update"));
        model.add(linkTo(methodOn(ContaAnuncianteController.class).deletar(conta.getId())).withRel("delete"));
        return model;
    }
}