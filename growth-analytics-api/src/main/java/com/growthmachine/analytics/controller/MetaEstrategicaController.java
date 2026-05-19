package com.growthmachine.analytics.controller;

import com.growthmachine.analytics.model.MetaEstrategica;
import com.growthmachine.analytics.service.MetaEstrategicaService;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/metas")
@RequiredArgsConstructor
@Tag(name = "Metas Estratégicas", description = "Endpoints para gestão de orçamentos e ROAS das contas")
public class MetaEstrategicaController {

    private final MetaEstrategicaService service;
    private final PagedResourcesAssembler<MetaEstrategica> assembler;

    @PostMapping
    @Operation(summary = "Criar nova Meta Estratégica", description = "Cria metas de orçamento e ROAS")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Meta criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação (ex: orçamento negativo)")
    })
    public ResponseEntity<EntityModel<MetaEstrategica>> criar(@Valid @RequestBody MetaEstrategica meta) {
        MetaEstrategica novaMeta = service.salvar(meta);
        return ResponseEntity.status(HttpStatus.CREATED).body(adicionarLinksHateoas(novaMeta));
    }

    @GetMapping
    @Operation(summary = "Listar todas as metas com paginação")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    public ResponseEntity<PagedModel<EntityModel<MetaEstrategica>>> listarTodos(@PageableDefault(size = 10) Pageable pageable) {
        Page<MetaEstrategica> metas = service.listarTodos(pageable);
        return ResponseEntity.ok(assembler.toModel(metas, this::adicionarLinksHateoas));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar meta por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meta encontrada"),
            @ApiResponse(responseCode = "404", description = "Meta não encontrada")
    })
    public ResponseEntity<EntityModel<MetaEstrategica>> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(m -> ResponseEntity.ok(adicionarLinksHateoas(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar uma meta existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meta atualizada"),
            @ApiResponse(responseCode = "404", description = "Meta não encontrada"),
            @ApiResponse(responseCode = "400", description = "Erro de validação")
    })
    public ResponseEntity<EntityModel<MetaEstrategica>> atualizar(@PathVariable Long id, @Valid @RequestBody MetaEstrategica metaAtualizada) {
        return service.buscarPorId(id).map(metaExistente -> {
            metaExistente.setOrcamentoMensal(metaAtualizada.getOrcamentoMensal());
            metaExistente.setRoasAlvo(metaAtualizada.getRoasAlvo());
            metaExistente.setContaAnunciante(metaAtualizada.getContaAnunciante());

            MetaEstrategica salva = service.salvar(metaExistente);
            return ResponseEntity.ok(adicionarLinksHateoas(salva));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar uma meta por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Meta deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Meta não encontrada")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (service.buscarPorId(id).isPresent()) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private EntityModel<MetaEstrategica> adicionarLinksHateoas(MetaEstrategica meta) {
        return EntityModel.of(meta,
                linkTo(methodOn(MetaEstrategicaController.class).buscarPorId(meta.getId())).withSelfRel(),
                linkTo(methodOn(MetaEstrategicaController.class).listarTodos(Pageable.unpaged())).withRel("todas_metas"),
                linkTo(methodOn(MetaEstrategicaController.class).atualizar(meta.getId(), meta)).withRel("update"),
                linkTo(methodOn(MetaEstrategicaController.class).deletar(meta.getId())).withRel("delete")
        );
    }
}