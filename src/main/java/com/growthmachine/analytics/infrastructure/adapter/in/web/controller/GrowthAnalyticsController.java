package com.growthmachine.analytics.infrastructure.adapter.in.web.controller;

import com.growthmachine.analytics.application.dto.IndicadoresCampanhaResponse;
import com.growthmachine.analytics.application.dto.SugestaoAnaliticaResponse;
import com.growthmachine.analytics.application.service.GrowthAnalyticsService;
import com.growthmachine.analytics.domain.exception.ErrorResponse;
import com.growthmachine.analytics.domain.model.SugestaoOtimizacao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics de Growth", description = "Endpoints orientados a mercado: consolidam dados mensurados, transformam em indicadores e geram sugestões acionáveis")
public class GrowthAnalyticsController {

    private final GrowthAnalyticsService service;

    @GetMapping("/campanhas/{campanhaId}/indicadores")
    @Operation(
            summary = "Consolidar indicadores de campanha",
            description = "Transforma métricas diárias mensuradas em KPIs de decisão: CTR, taxa de conversão, CPC, CPA, ticket médio, ROAS, comparação com meta e diagnóstico executivo."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Indicadores consolidados com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = IndicadoresCampanhaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Período inválido ou parâmetros mal formatados.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Campanha não encontrada.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<IndicadoresCampanhaResponse>> indicadores(
            @PathVariable Long campanhaId,
            @RequestParam LocalDate dataInicio,
            @RequestParam LocalDate dataFim) {
        IndicadoresCampanhaResponse indicadores = service.calcularIndicadores(campanhaId, dataInicio, dataFim);
        return ResponseEntity.ok(toIndicadoresModel(indicadores));
    }

    @GetMapping("/campanhas/{campanhaId}/sugestao")
    @Operation(
            summary = "Gerar sugestão analítica sem salvar",
            description = "Analisa os indicadores do período e retorna uma recomendação justificada sem persistir no histórico. Útil para simulações e painéis."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sugestão analítica gerada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SugestaoAnaliticaResponse.class))),
            @ApiResponse(responseCode = "400", description = "Período inválido ou parâmetros mal formatados.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Campanha não encontrada.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<SugestaoAnaliticaResponse>> sugestao(
            @PathVariable Long campanhaId,
            @RequestParam LocalDate dataInicio,
            @RequestParam LocalDate dataFim) {
        SugestaoAnaliticaResponse sugestao = service.gerarSugestao(campanhaId, dataInicio, dataFim);
        return ResponseEntity.ok(toSugestaoAnaliticaModel(campanhaId, dataInicio, dataFim, sugestao));
    }

    @PostMapping("/campanhas/{campanhaId}/sugestoes")
    @Operation(
            summary = "Gerar e salvar sugestão de otimização",
            description = "Cria uma sugestão persistida a partir dos indicadores mensurados. Este endpoint é idempotente quando usado com X-Idempotency-Key."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sugestão gerada e salva com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SugestaoOtimizacao.class))),
            @ApiResponse(responseCode = "400", description = "Período inválido ou parâmetros mal formatados.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autorizado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Campanha não encontrada.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<EntityModel<SugestaoOtimizacao>> gerarESalvarSugestao(
            @PathVariable Long campanhaId,
            @RequestParam LocalDate dataInicio,
            @RequestParam LocalDate dataFim) {
        SugestaoOtimizacao sugestao = service.gerarESalvarSugestao(campanhaId, dataInicio, dataFim);
        EntityModel<SugestaoOtimizacao> model = EntityModel.of(sugestao);
        model.add(linkTo(methodOn(SugestaoOtimizacaoController.class).buscar(sugestao.getId())).withSelfRel());
        model.add(linkTo(methodOn(SugestaoOtimizacaoController.class).listar(Pageable.unpaged())).withRel("historico-sugestoes"));
        model.add(linkTo(methodOn(GrowthAnalyticsController.class).indicadores(campanhaId, dataInicio, dataFim)).withRel("indicadores-origem"));
        return ResponseEntity.status(HttpStatus.CREATED).body(model);
    }

    private EntityModel<IndicadoresCampanhaResponse> toIndicadoresModel(IndicadoresCampanhaResponse indicadores) {
        EntityModel<IndicadoresCampanhaResponse> model = EntityModel.of(indicadores);
        model.add(linkTo(methodOn(GrowthAnalyticsController.class).indicadores(
                indicadores.campanhaId(), indicadores.dataInicio(), indicadores.dataFim())).withSelfRel());
        model.add(linkTo(methodOn(GrowthAnalyticsController.class).sugestao(
                indicadores.campanhaId(), indicadores.dataInicio(), indicadores.dataFim())).withRel("sugestao"));
        model.add(linkTo(methodOn(CampanhaController.class).buscar(indicadores.campanhaId())).withRel("campanha"));
        return model;
    }

    private EntityModel<SugestaoAnaliticaResponse> toSugestaoAnaliticaModel(Long campanhaId, LocalDate dataInicio,
                                                                             LocalDate dataFim,
                                                                             SugestaoAnaliticaResponse sugestao) {
        EntityModel<SugestaoAnaliticaResponse> model = EntityModel.of(sugestao);
        model.add(linkTo(methodOn(GrowthAnalyticsController.class).sugestao(campanhaId, dataInicio, dataFim)).withSelfRel());
        model.add(linkTo(methodOn(GrowthAnalyticsController.class).gerarESalvarSugestao(campanhaId, dataInicio, dataFim)).withRel("salvar-sugestao"));
        model.add(linkTo(methodOn(GrowthAnalyticsController.class).indicadores(campanhaId, dataInicio, dataFim)).withRel("indicadores"));
        return model;
    }
}
