package com.growthmachine.analytics.controller;

import com.growthmachine.analytics.model.SugestaoOtimizacao;
import com.growthmachine.analytics.service.SugestaoOtimizacaoService;
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
@RequestMapping("/api/sugestoes")
@RequiredArgsConstructor
@Tag(name = "Sugestões", description = "Endpoints de otimização de tráfego")
public class SugestaoOtimizacaoController {

    private final SugestaoOtimizacaoService service;
    private final PagedResourcesAssembler<SugestaoOtimizacao> assembler;

    @PostMapping
    @Operation(summary = "Criar sugestão")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Criado"),
            @ApiResponse(responseCode = "400", description = "Erro de validação")
    })
    public ResponseEntity<EntityModel<SugestaoOtimizacao>> criar(@Valid @RequestBody SugestaoOtimizacao sugestao) {
        SugestaoOtimizacao nova = service.salvar(sugestao);
        return ResponseEntity.status(HttpStatus.CREATED).body(adicionarLinks(nova));
    }

    @GetMapping
    @Operation(summary = "Listar sugestões")
    public ResponseEntity<PagedModel<EntityModel<SugestaoOtimizacao>>> listar(@PageableDefault(size = 10) Pageable pageable) {
        Page<SugestaoOtimizacao> pagina = service.listarTodos(pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, this::adicionarLinks));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar por ID")
    public ResponseEntity<EntityModel<SugestaoOtimizacao>> buscar(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(s -> ResponseEntity.ok(adicionarLinks(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar sugestão")
    public ResponseEntity<EntityModel<SugestaoOtimizacao>> atualizar(@PathVariable Long id, @Valid @RequestBody SugestaoOtimizacao atualizada) {
        return service.buscarPorId(id).map(existente -> {
            existente.setDescricao(atualizada.getDescricao());
            existente.setTipoAcao(atualizada.getTipoAcao());
            existente.setCampanhas(atualizada.getCampanhas());

            SugestaoOtimizacao salva = service.salvar(existente);
            return ResponseEntity.ok(adicionarLinks(salva));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar sugestão")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (service.buscarPorId(id).isPresent()) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private EntityModel<SugestaoOtimizacao> adicionarLinks(SugestaoOtimizacao s) {
        return EntityModel.of(s,
                linkTo(methodOn(SugestaoOtimizacaoController.class).buscar(s.getId())).withSelfRel(),
                linkTo(methodOn(SugestaoOtimizacaoController.class).listar(Pageable.unpaged())).withRel("lista"),
                linkTo(methodOn(SugestaoOtimizacaoController.class).atualizar(s.getId(), s)).withRel("update"),
                linkTo(methodOn(SugestaoOtimizacaoController.class).deletar(s.getId())).withRel("delete")
        );
    }
}