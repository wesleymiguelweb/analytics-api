package com.growthmachine.analytics.model;

import com.growthmachine.analytics.model.enums.CanalOrigem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

@Entity
@Table(name = "tb_campanha")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidade que representa uma campanha de tráfego pago")
public class Campanha extends RepresentationModel<Campanha> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da campanha é obrigatório")
    @Column(nullable = false, length = 150)
    @Schema(description = "Nome de identificação da campanha", example = "Campanha Dia das Mães - Search")
    private String nomeCampanha;

    @NotNull(message = "O canal de origem é obrigatório")
    @Enumerated(EnumType.STRING) // Salva o nome (String) do Enum no banco, não o número
    @Column(nullable = false)
    @Schema(description = "Plataforma onde a campanha está rodando")
    private CanalOrigem canalOrigem;

    @NotNull(message = "A conta anunciante é obrigatória")
    @ManyToOne
    @JoinColumn(name = "conta_anunciante_id", nullable = false)
    @Schema(description = "A conta anunciante dona desta campanha")
    private ContaAnunciante contaAnunciante;
}