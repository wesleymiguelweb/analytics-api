package com.growthmachine.analytics.domain.model;

import com.growthmachine.analytics.domain.model.enums.CanalOrigem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_campanha")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidade que representa uma campanha de tráfego pago")
public class Campanha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da campanha é obrigatório")
    @Column(nullable = false, length = 120)
    @Schema(description = "Nome identificador da campanha", example = "Black Friday - Conversões")
    private String nomeCampanha;

    @NotNull(message = "O canal de origem é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Canal principal onde a campanha está rodando", example = "GOOGLE_ADS")
    private CanalOrigem canalOrigem;

    @NotNull(message = "A conta anunciante é obrigatória")
    @ManyToOne
    @JoinColumn(name = "conta_anunciante_id", nullable = false)
    @Schema(description = "A conta anunciante dona desta campanha")
    private ContaAnunciante contaAnunciante;

    @ManyToMany
    @JoinTable(
            name = "tb_campanha_plataforma",
            joinColumns = @JoinColumn(name = "campanha_id"),
            inverseJoinColumns = @JoinColumn(name = "plataforma_id")
    )
    @Builder.Default
    @Schema(description = "Plataformas relacionadas à campanha")
    private List<Plataforma> plataformas = new ArrayList<>();
}
