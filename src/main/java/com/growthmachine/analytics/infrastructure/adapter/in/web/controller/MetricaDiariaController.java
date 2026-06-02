package com.growthmachine.analytics.infrastructure.adapter.in.web.controller;

import com.growthmachine.analytics.domain.exception.ErrorResponse;
import com.growthmachine.analytics.domain.model.MetricaDiaria;
import com.growthmachine.analytics.application.service.MetricaDiariaService;
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
    @Operation(summary = "Registrar Nova Métrica", description = "Insere os dados brutos de um dia. O sistema calculará automaticamente o CPA, ROAS e CTR. Este endpoint é idempotente.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Métricas registradas e KPIs calculados com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetricaDiaria.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<MetricaDiaria>> criar(@Valid @RequestBody MetricaDiaria metrica) {
        MetricaDiaria nova = service.salvar(metrica);
        return ResponseEntity.status(HttpStatus.CREATED).body(toEntityModel(nova));
    }

    @GetMapping
    @Operation(summary = "Listar Histórico de Métricas", description = "Retorna a listagem paginada de todos os registros de desempenho.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PagedModel<EntityModel<MetricaDiaria>>> listar(@PageableDefault(size = 10) Pageable pageable) {
        Page<MetricaDiaria> pagina = service.listarTodos(pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, this::toEntityModel));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Métrica por ID", description = "Recupera os detalhes e cálculos de um dia específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Métrica encontrada.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetricaDiaria.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Métrica não encontrada.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<MetricaDiaria>> buscar(@PathVariable Long id) {
        MetricaDiaria metrica = service.buscarPorId(id);
        return ResponseEntity.ok(toEntityModel(metrica));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Registro Diário", description = "Altera os dados brutos. Os KPIs (CPA, ROAS, CTR) serão recalculados automaticamente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados atualizados e KPIs recalculados com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetricaDiaria.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos novos dados fornecidos.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado para atualização.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<MetricaDiaria>> atualizar(@PathVariable Long id, @Valid @RequestBody MetricaDiaria atualizada) {
        MetricaDiaria salva = service.atualizar(id, atualizada);
        return ResponseEntity.ok(toEntityModel(salva));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir Métrica", description = "Remove um registro diário permanentemente do banco.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Exclusão efetuada com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/busca-por-data")
    @Operation(summary = "Filtrar por Data", description = "Busca os resultados consolidados de um dia exato (Formato: YYYY-MM-DD).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PagedModel<EntityModel<MetricaDiaria>>> buscarPorData(
            @RequestParam LocalDate data,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MetricaDiaria> pagina = service.buscarPorData(data, pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, this::toEntityModel));
    }

    @GetMapping("/busca-por-periodo")
    @Operation(summary = "Filtrar por Período", description = "Busca os resultados consolidados de um intervalo de datas (Formato: YYYY-MM-DD).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso."),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PagedModel<EntityModel<MetricaDiaria>>> buscarPorPeriodo(
            @RequestParam LocalDate dataInicio,
            @RequestParam LocalDate dataFim,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MetricaDiaria> pagina = service.buscarPorDataEntre(dataInicio, dataFim, pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, this::toEntityModel));
    }

    private EntityModel<MetricaDiaria> toEntityModel(MetricaDiaria metrica) {
        EntityModel<MetricaDiaria> model = EntityModel.of(metrica);
        model.add(linkTo(methodOn(MetricaDiariaController.class).buscar(metrica.getId())).withSelfRel());
        model.add(linkTo(methodOn(MetricaDiariaController.class).listar(Pageable.unpaged())).withRel("lista"));
        model.add(linkTo(methodOn(MetricaDiariaController.class).atualizar(metrica.getId(), metrica)).withRel("update"));
        model.add(linkTo(methodOn(MetricaDiariaController.class).deletar(metrica.getId())).withRel("delete"));
        return model;
    }
}
