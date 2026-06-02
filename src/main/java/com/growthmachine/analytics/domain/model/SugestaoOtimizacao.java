package com.growthmachine.analytics.domain.model;

import com.growthmachine.analytics.domain.model.enums.TipoAcao;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_sugestao_otimizacao")
@Data
@Schema(description = "Entidade que representa uma recomendação de otimização para campanhas")
public class SugestaoOtimizacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A descrição da sugestão é obrigatória")
    @Column(nullable = false, length = 500)
    @Schema(description = "Descrição textual da recomendação de otimização", example = "Aumentar orçamento da campanha com ROAS acima da meta.")
    private String descricao;

    @NotNull(message = "O tipo de ação é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Tipo da ação recomendada", example = "AUMENTAR_ORCAMENTO")
    private TipoAcao tipoAcao;

    @ManyToMany
    @JoinTable(
            name = "tb_sugestao_campanha",
            joinColumns = @JoinColumn(name = "sugestao_id"),
            inverseJoinColumns = @JoinColumn(name = "campanha_id")
    )
    private List<Campanha> campanhas = new ArrayList<>();
}
