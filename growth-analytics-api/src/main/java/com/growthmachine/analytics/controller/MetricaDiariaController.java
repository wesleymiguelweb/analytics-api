package com.growthmachine.analytics.controller;

import com.growthmachine.analytics.exception.ErroResposta;
import com.growthmachine.analytics.model.MetricaDiaria;
import com.growthmachine.analytics.service.MetricaDiariaService;
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

import java.time.LocalDate;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/metricas")
@RequiredArgsConstructor
@Tag(name = "Métricas Diárias", description = "Endpoints para registro de desempenho e cálculo automático de KPIs (CPA, ROAS, CTR)")
public class MetricaDiariaController {

    private final MetricaDiariaService service;
    private final PagedResourcesAssembler<MetricaDiaria> assembler;

    @PostMapping
    @Operation(
            summary = "Registrar Nova Métrica",
            description = "Insere os dados brutos de um dia (Cliques, Impressões, Custo, Conversões, GMV). O sistema calculará automaticamente o CPA, ROAS e CTR antes de salvar."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Métricas registradas e KPIs calculados com sucesso.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetricaDiaria.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação (Ex: custo negativo, falta de ID da campanha).",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<EntityModel<MetricaDiaria>> criar(@Valid @RequestBody MetricaDiaria metrica) {
        MetricaDiaria nova = service.salvar(metrica);
        return ResponseEntity.status(HttpStatus.CREATED).body(adicionarLinks(nova));
    }

    @GetMapping
    @Operation(summary = "Listar Histórico de Métricas", description = "Retorna a listagem paginada de todos os registros de desempenho.")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso.")
    public ResponseEntity<PagedModel<EntityModel<MetricaDiaria>>> listar(@PageableDefault(size = 10) Pageable pageable) {
        Page<MetricaDiaria> pagina = service.listarTodos(pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, this::adicionarLinks));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Métrica por ID", description = "Recupera os detalhes e cálculos de um dia específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Métrica encontrada.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetricaDiaria.class))),
            @ApiResponse(responseCode = "404", description = "Métrica não encontrada.",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<EntityModel<MetricaDiaria>> buscar(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(m -> ResponseEntity.ok(adicionarLinks(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar Registro Diário",
            description = "Altera os dados brutos. Os KPIs (CPA, ROAS, CTR) serão recalculados automaticamente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados atualizados e KPIs recalculados com sucesso.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetricaDiaria.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos novos dados fornecidos.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado para atualização.",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<EntityModel<MetricaDiaria>> atualizar(@PathVariable Long id, @Valid @RequestBody MetricaDiaria atualizada) {
        return service.buscarPorId(id).map(existente -> {
            existente.setData(atualizada.getData());
            existente.setCliques(atualizada.getCliques());
            existente.setImpressoes(atualizada.getImpressoes());
            existente.setCusto(atualizada.getCusto());
            existente.setConversoes(atualizada.getConversoes());
            existente.setGmv(atualizada.getGmv());
            existente.setCampanha(atualizada.getCampanha());

            MetricaDiaria salva = service.salvar(existente);
            return ResponseEntity.ok(adicionarLinks(salva));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir Métrica", description = "Remove um registro diário permanentemente do banco.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Exclusão efetuada com sucesso."),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado.")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (service.buscarPorId(id).isPresent()) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/busca-por-data")
    @Operation(summary = "Filtrar por Data", description = "Busca os resultados consolidados de um dia exato (Formato: YYYY-MM-DD).")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso.")
    public ResponseEntity<PagedModel<EntityModel<MetricaDiaria>>> buscarPorData(
            @RequestParam LocalDate data,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MetricaDiaria> pagina = service.buscarPorData(data, pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, this::adicionarLinks));
    }

    private EntityModel<MetricaDiaria> adicionarLinks(MetricaDiaria m) {
        return EntityModel.of(m,
                linkTo(methodOn(MetricaDiariaController.class).buscar(m.getId())).withSelfRel(),
                linkTo(methodOn(MetricaDiariaController.class).listar(Pageable.unpaged())).withRel("lista"),
                linkTo(methodOn(MetricaDiariaController.class).atualizar(m.getId(), m)).withRel("update"),
                linkTo(methodOn(MetricaDiariaController.class).deletar(m.getId())).withRel("delete")
        );
    }
}