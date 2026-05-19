package com.growthmachine.analytics.controller;

import com.growthmachine.analytics.exception.ErroResposta;
import com.growthmachine.analytics.model.MetaEstrategica;
import com.growthmachine.analytics.service.MetaEstrategicaService;
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
@RequestMapping("/api/metas")
@RequiredArgsConstructor
@Tag(name = "Metas Estratégicas", description = "Endpoints para definição de orçamento e ROAS alvo por Anunciante")
public class MetaEstrategicaController {

    private final MetaEstrategicaService service;
    private final PagedResourcesAssembler<MetaEstrategica> assembler;

    @PostMapping
    @Operation(summary = "Criar nova Meta", description = "Define o orçamento mensal e o ROAS alvo para uma Conta Anunciante.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Meta criada com sucesso.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetaEstrategica.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação (Ex: orçamento negativo ou falta de conta vinculada).",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<EntityModel<MetaEstrategica>> criar(@Valid @RequestBody MetaEstrategica meta) {
        MetaEstrategica nova = service.salvar(meta);
        return ResponseEntity.status(HttpStatus.CREATED).body(adicionarLinks(nova));
    }

    @GetMapping
    @Operation(summary = "Listar todas as Metas", description = "Retorna uma lista paginada com as diretrizes estratégicas ativas.")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso.")
    public ResponseEntity<PagedModel<EntityModel<MetaEstrategica>>> listar(@PageableDefault(size = 10) Pageable pageable) {
        Page<MetaEstrategica> pagina = service.listarTodos(pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, this::adicionarLinks));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Meta por ID", description = "Recupera os detalhes de uma meta estratégica específica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Meta encontrada.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetaEstrategica.class))),
            @ApiResponse(responseCode = "404", description = "Meta não encontrada no banco de dados.",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<EntityModel<MetaEstrategica>> buscar(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(m -> ResponseEntity.ok(adicionarLinks(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Meta Estratégica", description = "Altera os valores de orçamento ou ROAS alvo de uma meta existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Meta atualizada com sucesso.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetaEstrategica.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos novos dados enviados.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "404", description = "Meta não encontrada para atualização.",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<EntityModel<MetaEstrategica>> atualizar(@PathVariable Long id, @Valid @RequestBody MetaEstrategica atualizada) {
        return service.buscarPorId(id).map(existente -> {
            existente.setOrcamentoMensal(atualizada.getOrcamentoMensal());
            existente.setRoasAlvo(atualizada.getRoasAlvo());
            existente.setContaAnunciante(atualizada.getContaAnunciante());

            MetaEstrategica salva = service.salvar(existente);
            return ResponseEntity.ok(adicionarLinks(salva));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir Meta", description = "Remove uma meta estratégica do sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Exclusão efetuada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Meta não encontrada.")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (service.buscarPorId(id).isPresent()) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/busca-por-roas")
    @Operation(summary = "Filtrar por ROAS Mínimo", description = "Busca metas que possuam um ROAS alvo maior ou igual ao valor informado.")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso.")
    public ResponseEntity<PagedModel<EntityModel<MetaEstrategica>>> buscarPorRoasMinimo(
            @RequestParam Double roas,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MetaEstrategica> pagina = service.buscarPorRoasMinimo(roas, pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, this::adicionarLinks));
    }

    private EntityModel<MetaEstrategica> adicionarLinks(MetaEstrategica m) {
        return EntityModel.of(m,
                linkTo(methodOn(MetaEstrategicaController.class).buscar(m.getId())).withSelfRel(),
                linkTo(methodOn(MetaEstrategicaController.class).listar(Pageable.unpaged())).withRel("lista"),
                linkTo(methodOn(MetaEstrategicaController.class).atualizar(m.getId(), m)).withRel("update"),
                linkTo(methodOn(MetaEstrategicaController.class).deletar(m.getId())).withRel("delete")
        );
    }
}