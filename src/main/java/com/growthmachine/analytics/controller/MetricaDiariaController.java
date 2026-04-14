package com.growthmachine.analytics.controller;

import com.growthmachine.analytics.model.MetricaDiaria;
import com.growthmachine.analytics.service.MetricaDiariaService;
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
@RequestMapping("/api/metricas")
@RequiredArgsConstructor
@Tag(name = "Métricas Diárias", description = "Endpoints para registro de dados e cálculo automático de performance")
public class MetricaDiariaController {

    private final MetricaDiariaService service;
    private final PagedResourcesAssembler<MetricaDiaria> assembler;

    @PostMapping
    @Operation(summary = "Registrar novas métricas diárias", description = "Salva os dados brutos. O sistema calcula ROAS, CPA e CTR automaticamente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Métricas registradas com sucesso"),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos dados enviados")
    })
    public ResponseEntity<EntityModel<MetricaDiaria>> criar(@Valid @RequestBody MetricaDiaria metrica) {
        MetricaDiaria novaMetrica = service.salvar(metrica);
        return ResponseEntity.status(HttpStatus.CREATED).body(adicionarLinksHateoas(novaMetrica));
    }

    @GetMapping
    @Operation(summary = "Listar todas as métricas com paginação")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso")
    public ResponseEntity<PagedModel<EntityModel<MetricaDiaria>>> listarTodos(@PageableDefault(size = 10) Pageable pageable) {
        Page<MetricaDiaria> metricas = service.listarTodos(pageable);
        return ResponseEntity.ok(assembler.toModel(metricas, this::adicionarLinksHateoas));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar métrica por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métrica encontrada"),
            @ApiResponse(responseCode = "404", description = "Métrica não encontrada")
    })
    public ResponseEntity<EntityModel<MetricaDiaria>> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(m -> ResponseEntity.ok(adicionarLinksHateoas(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar métricas existentes")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métrica atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Métrica não encontrada"),
            @ApiResponse(responseCode = "400", description = "Erro de validação")
    })
    public ResponseEntity<EntityModel<MetricaDiaria>> atualizar(@PathVariable Long id, @Valid @RequestBody MetricaDiaria metricaAtualizada) {
        return service.buscarPorId(id).map(metricaExistente -> {
            metricaExistente.setData(metricaAtualizada.getData());
            metricaExistente.setCliques(metricaAtualizada.getCliques());
            metricaExistente.setImpressoes(metricaAtualizada.getImpressoes());
            metricaExistente.setCusto(metricaAtualizada.getCusto());
            metricaExistente.setConversoes(metricaAtualizada.getConversoes());
            metricaExistente.setGmv(metricaAtualizada.getGmv());
            metricaExistente.setCampanha(metricaAtualizada.getCampanha());

            MetricaDiaria salva = service.salvar(metricaExistente);
            return ResponseEntity.ok(adicionarLinksHateoas(salva));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar métrica por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Métrica deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Métrica não encontrada")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (service.buscarPorId(id).isPresent()) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private EntityModel<MetricaDiaria> adicionarLinksHateoas(MetricaDiaria metrica) {
        return EntityModel.of(metrica,
                linkTo(methodOn(MetricaDiariaController.class).buscarPorId(metrica.getId())).withSelfRel(),
                linkTo(methodOn(MetricaDiariaController.class).listarTodos(Pageable.unpaged())).withRel("todas_metricas"),
                linkTo(methodOn(MetricaDiariaController.class).atualizar(metrica.getId(), metrica)).withRel("update"),
                linkTo(methodOn(MetricaDiariaController.class).deletar(metrica.getId())).withRel("delete")
        );
    }
}