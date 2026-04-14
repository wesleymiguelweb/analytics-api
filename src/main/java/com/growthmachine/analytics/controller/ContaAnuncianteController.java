package com.growthmachine.analytics.controller;

import com.growthmachine.analytics.model.ContaAnunciante;
import com.growthmachine.analytics.service.ContaAnuncianteService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/contas")
@RequiredArgsConstructor
@Tag(name = "Contas Anunciantes", description = "Endpoints para gestão das empresas (Anunciantes)")
public class ContaAnuncianteController {

    private final ContaAnuncianteService service;
    // Ferramenta injetada para transformar Pages em PagedModel (Requisito HATEOAS)
    private final PagedResourcesAssembler<ContaAnunciante> assembler;

    @PostMapping
    @Operation(summary = "Criar nova Conta Anunciante", description = "Cria uma nova empresa e retorna os dados cadastrados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conta criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados enviados")
    })
    public ResponseEntity<EntityModel<ContaAnunciante>> criar(@Valid @RequestBody ContaAnunciante conta) {
        ContaAnunciante novaConta = service.salvar(conta);
        return ResponseEntity.status(HttpStatus.CREATED).body(adicionarLinksHateoas(novaConta));
    }

    @GetMapping
    @Operation(summary = "Listar todas as contas com paginação", description = "Retorna uma lista paginada de contas")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    public ResponseEntity<PagedModel<EntityModel<ContaAnunciante>>> listarTodos(@PageableDefault(size = 10) Pageable pageable) {
        Page<ContaAnunciante> contas = service.listarTodos(pageable);

        // Transforma o Page normal em um PagedModel do HATEOAS (com links de next/prev page)
        PagedModel<EntityModel<ContaAnunciante>> pagedModel = assembler.toModel(contas, this::adicionarLinksHateoas);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar conta por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta encontrada"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<EntityModel<ContaAnunciante>> buscarPorId(@PathVariable Long id) {
        Optional<ContaAnunciante> conta = service.buscarPorId(id);
        return conta.map(c -> ResponseEntity.ok(adicionarLinksHateoas(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar uma conta existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada"),
            @ApiResponse(responseCode = "400", description = "Erro de validação")
    })
    public ResponseEntity<EntityModel<ContaAnunciante>> atualizar(@PathVariable Long id, @Valid @RequestBody ContaAnunciante contaAtualizada) {
        return service.buscarPorId(id).map(contaExistente -> {
            contaExistente.setNomeEmpresa(contaAtualizada.getNomeEmpresa());
            ContaAnunciante salva = service.salvar(contaExistente);
            return ResponseEntity.ok(adicionarLinksHateoas(salva));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar uma conta por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Conta não encontrada")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (service.buscarPorId(id).isPresent()) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // Método que constrói o EntityModel com ALL os links exigidos (self, update, delete)
    private EntityModel<ContaAnunciante> adicionarLinksHateoas(ContaAnunciante conta) {
        return EntityModel.of(conta,
                linkTo(methodOn(ContaAnuncianteController.class).buscarPorId(conta.getId())).withSelfRel(),
                linkTo(methodOn(ContaAnuncianteController.class).listarTodos(Pageable.unpaged())).withRel("todas_contas"),
                linkTo(methodOn(ContaAnuncianteController.class).atualizar(conta.getId(), conta)).withRel("update"),
                linkTo(methodOn(ContaAnuncianteController.class).deletar(conta.getId())).withRel("delete")
        );
    }
}