package com.growthmachine.analytics.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Indicadores consolidados de performance de uma campanha em um período")
public record IndicadoresCampanhaResponse(
        @Schema(description = "ID da campanha analisada", example = "1")
        Long campanhaId,

        @Schema(description = "Nome da campanha analisada", example = "Black Friday - Conversões")
        String campanha,

        @Schema(description = "Data inicial do período analisado", example = "2026-03-01")
        LocalDate dataInicio,

        @Schema(description = "Data final do período analisado", example = "2026-03-31")
        LocalDate dataFim,

        @Schema(description = "Quantidade de registros diários considerados", example = "31")
        long diasMensurados,

        @Schema(description = "Total de impressões no período", example = "120000")
        int impressoes,

        @Schema(description = "Total de cliques no período", example = "3600")
        int cliques,

        @Schema(description = "Total de conversões no período", example = "145")
        int conversoes,

        @Schema(description = "Custo total no período", example = "4500.00")
        BigDecimal custo,

        @Schema(description = "GMV total no período", example = "19800.00")
        BigDecimal gmv,

        @Schema(description = "CTR percentual", example = "3.00")
        BigDecimal ctr,

        @Schema(description = "Taxa de conversão percentual", example = "4.03")
        BigDecimal taxaConversao,

        @Schema(description = "Custo médio por clique", example = "1.25")
        BigDecimal cpc,

        @Schema(description = "Custo por aquisição", example = "31.03")
        BigDecimal cpa,

        @Schema(description = "Ticket médio por conversão", example = "136.55")
        BigDecimal ticketMedio,

        @Schema(description = "Retorno sobre investimento publicitário", example = "4.40")
        BigDecimal roas,

        @Schema(description = "ROAS alvo configurado na meta estratégica da conta", example = "3.50")
        BigDecimal roasAlvo,

        @Schema(description = "Diferença entre ROAS realizado e ROAS alvo", example = "0.90")
        BigDecimal margemRoas,

        @Schema(description = "Diagnóstico executivo da campanha", example = "ESCALAR")
        String diagnostico,

        @Schema(description = "Alertas objetivos encontrados nos dados")
        List<String> alertas
) {
}
