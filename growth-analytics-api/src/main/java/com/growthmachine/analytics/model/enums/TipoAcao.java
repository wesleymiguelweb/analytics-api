package com.growthmachine.analytics.model.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipos de ações recomendadas pelo motor de Growth")
public enum TipoAcao {
    AUMENTAR_ORCAMENTO,
    REDUZIR_ORCAMENTO,
    PAUSAR_CAMPANHA,
    OTIMIZAR_PALAVRAS_CHAVE,
    AJUSTAR_PUBLICO_ALVO
}