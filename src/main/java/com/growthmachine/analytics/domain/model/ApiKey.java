package com.growthmachine.analytics.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_api_key")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiKey {

    @Id
    @NotBlank(message = "A chave de API é obrigatória")
    @Schema(description = "Valor da chave que deve ser enviado no header X-API-Key", example = "analytics-dev-key-2026")
    private String apiKey;

    @NotBlank(message = "O proprietário da chave é obrigatório")
    @Schema(description = "Identificação do proprietário da chave", example = "wesley")
    private String owner;
}
