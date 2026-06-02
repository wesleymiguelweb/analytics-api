package com.growthmachine.analytics.application.dto;

import com.growthmachine.analytics.domain.model.enums.TipoAcao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Recomendação gerada a partir dos indicadores mensurados")
public record SugestaoAnaliticaResponse(
        @Schema(description = "Tipo de ação recomendada", example = "AUMENTAR_ORCAMENTO")
        TipoAcao tipoAcao,

        @Schema(description = "Texto objetivo da recomendação", example = "Aumentar orçamento em 15% porque o ROAS está acima da meta com volume consistente de conversões.")
        String descricao,

        @Schema(description = "Indicadores usados como evidência")
        IndicadoresCampanhaResponse indicadores,

        @Schema(description = "Motivos que sustentam a recomendação")
        List<String> justificativas
) {
}
