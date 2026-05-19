package com.growthmachine.analytics.model;

import com.growthmachine.analytics.model.enums.TipoAcao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.util.List;

@Entity
@Table(name = "tb_sugestao_otimizacao")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidade de recomendações automáticas")
public class SugestaoOtimizacao extends RepresentationModel<SugestaoOtimizacao> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "A descrição é obrigatória")
    @Schema(example = "Aumentar orçamento em 20% devido ao ROAS alto")
    private String descricao;

    @NotNull(message = "O tipo de ação é obrigatório")
    @Enumerated(EnumType.STRING)
    private TipoAcao tipoAcao;

    @ManyToMany
    @JoinTable(
            name = "tb_campanha_sugestao",
            joinColumns = @JoinColumn(name = "sugestao_id"),
            inverseJoinColumns = @JoinColumn(name = "campanha_id")
    )
    private List<Campanha> campanhas;
}