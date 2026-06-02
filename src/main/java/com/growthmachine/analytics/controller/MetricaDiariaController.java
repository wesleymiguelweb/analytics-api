package com.growthmachine.analytics.controller;

import com.growthmachine.analytics.model.MetricaDiaria;
import com.growthmachine.analytics.service.MetricaDiariaService;
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

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/metricas")
@RequiredArgsConstructor
@Tag(name = "Métricas Diárias", description = "Endpoints para registro de desempenho e cálculo automático de KPIs (CPA, ROAS, CTR)")
public class MetricaDiariaController {

    private final MetricaDiariaService service;
    private final PagedResourcesAssembler<MetricaDiaria> assembler;

    @PostMapping
    @Operation(summary = "Registrar Nova Métrica", description = "Insere os dados brutos de um dia. O sistema calculará automaticamente o CPA, ROAS e CTR. Este endpoint é idempotente.")
    @Parameter(in = ParameterIn.HEADER, name = "X-Idempotency-Key", description = "Chave para garantir que a operação não seja executada duas vezes.", required = false)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Métricas registradas e KPIs calculados com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetricaDiaria.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<EntityModel<MetricaDiaria>> criar(@Valid @RequestBody MetricaDiaria metrica) {
        MetricaDiaria nova = service.salvar(metrica);
        EntityModel<MetricaDiaria> entityModel = EntityModel.of(nova);
        nova.addLinks(entityModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @GetMapping
    @Operation(summary = "Listar Histórico de Métricas", description = "Retorna a listagem paginada de todos os registros de desempenho.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<PagedModel<EntityModel<MetricaDiaria>>> listar(@PageableDefault(size = 10) Pageable pageable) {
        Page<MetricaDiaria> pagina = service.listarTodos(pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, metrica -> {
            EntityModel<MetricaDiaria> entityModel = EntityModel.of(metrica);
            metrica.addLinks(entityModel);
            return entityModel;
        }));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Métrica por ID", description = "Recupera os detalhes e cálculos de um dia específico.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Métrica encontrada.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetricaDiaria.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Métrica não encontrada.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<EntityModel<MetricaDiaria>> buscar(@PathVariable Long id) {
        MetricaDiaria metrica = service.buscarPorId(id);
        EntityModel<MetricaDiaria> entityModel = EntityModel.of(metrica);
        metrica.addLinks(entityModel);
        return ResponseEntity.ok(entityModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Registro Diário", description = "Altera os dados brutos. Os KPIs (CPA, ROAS, CTR) serão recalculados automaticamente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dados atualizados e KPIs recalculados com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetricaDiaria.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos novos dados fornecidos.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado para atualização.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<EntityModel<MetricaDiaria>> atualizar(@PathVariable Long id, @Valid @RequestBody MetricaDiaria atualizada) {
        MetricaDiaria salva = service.atualizar(id, atualizada);
        EntityModel<MetricaDiaria> entityModel = EntityModel.of(salva);
        salva.addLinks(entityModel);
        return ResponseEntity.ok(entityModel);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir Métrica", description = "Remove um registro diário permanentemente do banco.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Exclusão efetuada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Registro não encontrado."),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/busca-por-data")
    @Operation(summary = "Filtrar por Data", description = "Busca os resultados consolidados de um dia exato (Formato: YYYY-MM-DD).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<PagedModel<EntityModel<MetricaDiaria>>> buscarPorData(
            @RequestParam LocalDate data,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MetricaDiaria> pagina = service.buscarPorData(data, pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, metrica -> {
            EntityModel<MetricaDiaria> entityModel = EntityModel.of(metrica);
            metrica.addLinks(entityModel);
            return entityModel;
        }));
    }
}