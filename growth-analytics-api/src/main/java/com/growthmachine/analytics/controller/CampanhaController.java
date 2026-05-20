package com.growthmachine.analytics.controller;

import com.growthmachine.analytics.exception.ErroResposta;
import com.growthmachine.analytics.model.Campanha;
import com.growthmachine.analytics.service.CampanhaService;
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
    @Operation(summary = "Criar nova Campanha", description = "Cadastra uma nova campanha no sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Campanha criada com sucesso.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Campanha.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<EntityModel<Campanha>> criar(@Valid @RequestBody Campanha campanha) {
        Campanha nova = service.salvar(campanha);
        return ResponseEntity.status(HttpStatus.CREATED).body(adicionarLinks(nova));
    }

    @GetMapping
    @Operation(summary = "Listar todas as Campanhas", description = "Retorna uma lista paginada.")
    @ApiResponse(responseCode = "200", description = "Sucesso.")
    public ResponseEntity<PagedModel<EntityModel<Campanha>>> listar(@PageableDefault(size = 10) Pageable pageable) {
        Page<Campanha> pagina = service.listarTodos(pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, this::adicionarLinks));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Campanha por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Encontrada."),
            @ApiResponse(responseCode = "404", description = "Campanha não encontrada.")
    })
    public ResponseEntity<EntityModel<Campanha>> buscar(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(c -> ResponseEntity.ok(adicionarLinks(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir Campanha")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Excluída com sucesso."),
            @ApiResponse(responseCode = "404", description = "Campanha não encontrada.")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (service.buscarPorId(id).isPresent()) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    private EntityModel<Campanha> adicionarLinks(Campanha c) {
        return EntityModel.of(c,
                linkTo(methodOn(CampanhaController.class).buscar(c.getId())).withSelfRel(),
                linkTo(methodOn(CampanhaController.class).listar(Pageable.unpaged())).withRel("lista"),
                linkTo(methodOn(CampanhaController.class).deletar(c.getId())).withRel("delete")
        );
    }
}