package com.growthmachine.analytics.controller;

import com.growthmachine.analytics.model.ContaAnunciante;
import com.growthmachine.analytics.service.ContaAnuncianteService;
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
@RequestMapping("/api/contas")
@RequiredArgsConstructor
@Tag(name = "Contas Anunciantes", description = "Endpoints para gestão das empresas (Anunciantes) no sistema")
public class ContaAnuncianteController {

    private final ContaAnuncianteService service;
    private final PagedResourcesAssembler<ContaAnunciante> assembler;

    @PostMapping
    @Operation(summary = "Criar nova Conta Anunciante", description = "Cadastra uma nova empresa no sistema. Este endpoint é idempotente.")
    @Parameter(in = ParameterIn.HEADER, name = "X-Idempotency-Key", description = "Chave para garantir que a operação não seja executada duas vezes.", required = false)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta criada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ContaAnunciante.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados enviados.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<EntityModel<ContaAnunciante>> criar(@Valid @RequestBody ContaAnunciante conta) {
        ContaAnunciante nova = service.salvar(conta);
        EntityModel<ContaAnunciante> entityModel = EntityModel.of(nova);
        nova.addLinks(entityModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @GetMapping
    @Operation(summary = "Listar todas as Contas", description = "Retorna uma lista paginada de todas as contas anunciantes.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<PagedModel<EntityModel<ContaAnunciante>>> listar(@PageableDefault(size = 10) Pageable pageable) {
        Page<ContaAnunciante> pagina = service.listarTodos(pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, conta -> {
            EntityModel<ContaAnunciante> entityModel = EntityModel.of(conta);
            conta.addLinks(entityModel);
            return entityModel;
        }));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Conta por ID", description = "Recupera os detalhes de uma conta específica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta encontrada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ContaAnunciante.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada no banco de dados.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<EntityModel<ContaAnunciante>> buscar(@PathVariable Long id) {
        ContaAnunciante conta = service.buscarPorId(id);
        EntityModel<ContaAnunciante> entityModel = EntityModel.of(conta);
        conta.addLinks(entityModel);
        return ResponseEntity.ok(entityModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Conta", description = "Altera os dados de uma empresa existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta atualizada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ContaAnunciante.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados enviados.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada para atualização.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<EntityModel<ContaAnunciante>> atualizar(@PathVariable Long id, @Valid @RequestBody ContaAnunciante atualizada) {
        ContaAnunciante salva = service.atualizar(id, atualizada);
        EntityModel<ContaAnunciante> entityModel = EntityModel.of(salva);
        salva.addLinks(entityModel);
        return ResponseEntity.ok(entityModel);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir Conta", description = "Remove permanentemente uma conta do sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Conta excluída com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/busca-por-nome")
    @Operation(summary = "Filtrar por Nome da Empresa", description = "Busca contas anunciantes que contenham o termo especificado no nome.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<PagedModel<EntityModel<ContaAnunciante>>> buscarPorNome(
            @RequestParam String nome,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ContaAnunciante> pagina = service.buscarPorNome(nome, pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, conta -> {
            EntityModel<ContaAnunciante> entityModel = EntityModel.of(conta);
            conta.addLinks(entityModel);
            return entityModel;
        }));
    }
}