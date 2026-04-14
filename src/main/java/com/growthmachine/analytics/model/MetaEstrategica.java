package com.growthmachine.analytics.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;

@Entity
@Table(name = "tb_meta_estrategica")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidade que representa as metas financeiras e de performance de uma conta")
public class MetaEstrategica extends RepresentationModel<MetaEstrategica> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "O orçamento mensal não pode ser nulo")
    @PositiveOrZero(message = "O orçamento não pode ser negativo")
    @Column(nullable = false, precision = 10, scale = 2)
    @Schema(description = "Orçamento máximo estipulado para o mês", example = "5000.00")
    private BigDecimal orcamentoMensal;

    @NotNull(message = "O ROAS alvo não pode ser nulo")
    @PositiveOrZero(message = "O ROAS alvo não pode ser negativo")
    @Column(nullable = false, precision = 5, scale = 2)
    @Schema(description = "Retorno sobre investimento publicitário (ROAS) esperado", example = "3.50")
    private BigDecimal roasAlvo;

    // Relacionamento 1:1 com a Conta Anunciante
    @OneToOne
    @JoinColumn(name = "conta_anunciante_id", referencedColumnName = "id", unique = true, nullable = false)
    @Schema(description = "A conta anunciante dona desta meta")
    private ContaAnunciante contaAnunciante;
}