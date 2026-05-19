package com.growthmachine.analytics.controller;

import com.growthmachine.analytics.exception.ErroResposta;
import com.growthmachine.analytics.model.ContaAnunciante;
import com.growthmachine.analytics.service.ContaAnuncianteService;
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
    @Operation(summary = "Criar nova Conta Anunciante", description = "Cadastra uma nova empresa no sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Conta criada com sucesso.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ContaAnunciante.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação (Ex: nome da empresa não informado).",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<EntityModel<ContaAnunciante>> criar(@Valid @RequestBody ContaAnunciante conta) {
        ContaAnunciante nova = service.salvar(conta);
        return ResponseEntity.status(HttpStatus.CREATED).body(adicionarLinks(nova));
    }

    @GetMapping
    @Operation(summary = "Listar todas as Contas", description = "Retorna uma lista paginada de todas as contas anunciantes.")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso.")
    public ResponseEntity<PagedModel<EntityModel<ContaAnunciante>>> listar(@PageableDefault(size = 10) Pageable pageable) {
        Page<ContaAnunciante> pagina = service.listarTodos(pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, this::adicionarLinks));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Conta por ID", description = "Recupera os detalhes de uma conta específica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta encontrada com sucesso.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ContaAnunciante.class))),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada no banco de dados.",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<EntityModel<ContaAnunciante>> buscar(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(c -> ResponseEntity.ok(adicionarLinks(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Conta", description = "Altera os dados de uma empresa existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Conta atualizada com sucesso.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ContaAnunciante.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados enviados.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada para atualização.",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<EntityModel<ContaAnunciante>> atualizar(@PathVariable Long id, @Valid @RequestBody ContaAnunciante atualizada) {
        return service.buscarPorId(id).map(existente -> {
            existente.setNomeEmpresa(atualizada.getNomeEmpresa());
            ContaAnunciante salva = service.salvar(existente);
            return ResponseEntity.ok(adicionarLinks(salva));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir Conta", description = "Remove permanentemente uma conta do sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Conta excluída com sucesso."),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada.")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (service.buscarPorId(id).isPresent()) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/busca-por-nome")
    @Operation(summary = "Filtrar por Nome da Empresa", description = "Busca contas anunciantes que contenham o termo especificado no nome.")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso.")
    public ResponseEntity<PagedModel<EntityModel<ContaAnunciante>>> buscarPorNome(
            @RequestParam String nome,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ContaAnunciante> pagina = service.buscarPorNome(nome, pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, this::adicionarLinks));
    }

    private EntityModel<ContaAnunciante> adicionarLinks(ContaAnunciante c) {
        return EntityModel.of(c,
                linkTo(methodOn(ContaAnuncianteController.class).buscar(c.getId())).withSelfRel(),
                linkTo(methodOn(ContaAnuncianteController.class).listar(Pageable.unpaged())).withRel("lista"),
                linkTo(methodOn(ContaAnuncianteController.class).atualizar(c.getId(), c)).withRel("update"),
                linkTo(methodOn(ContaAnuncianteController.class).deletar(c.getId())).withRel("delete")
        );
    }
}