package com.growthmachine.analytics.model;

import com.growthmachine.analytics.controller.HateoasLinkBuilder;
import com.growthmachine.analytics.controller.MetricaDiariaController;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Entity
@Table(name = "tb_metrica_diaria")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidade que armazena os dados brutos diários e calcula métricas de performance (Lógica de Growth)")
public class MetricaDiaria extends RepresentationModel<MetricaDiaria> implements HateoasLinkBuilder<MetricaDiaria> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "A data da métrica é obrigatória")
    @Column(nullable = false)
    @Schema(description = "Data da consolidação dos dados", example = "2026-03-30")
    private LocalDate data;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer cliques;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer impressoes;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal custo;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer conversoes;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 10, scale = 2)
    @Schema(description = "Gross Merchandise Volume - Total faturado em vendas", example = "135.60")
    private BigDecimal gmv;

    @ManyToOne
    @JoinColumn(name = "campanha_id", nullable = false)
    private Campanha campanha;

    @Schema(description = "Retorno sobre Investimento (ROAS) calculado automaticamente", accessMode = Schema.AccessMode.READ_ONLY)
    public BigDecimal getRoas() {
        if (custo == null || custo.compareTo(BigDecimal.ZERO) == 0 || gmv == null) {
            return BigDecimal.ZERO;
        }
        return gmv.divide(custo, 2, RoundingMode.HALF_UP);
    }

    @Schema(description = "Custo por Aquisição (CPA) calculado automaticamente", accessMode = Schema.AccessMode.READ_ONLY)
    public BigDecimal getCpa() {
        if (conversoes == null || conversoes == 0 || custo == null) {
            return BigDecimal.ZERO;
        }
        return custo.divide(new BigDecimal(conversoes), 2, RoundingMode.HALF_UP);
    }

    @Schema(description = "Click-Through Rate (CTR) percentual calculado automaticamente", accessMode = Schema.AccessMode.READ_ONLY)
    public BigDecimal getCtr() {
        if (impressoes == null || impressoes == 0 || cliques == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(cliques)
                .divide(new BigDecimal(impressoes), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public void addLinks(EntityModel<MetricaDiaria> model) {
        model.add(linkTo(methodOn(MetricaDiariaController.class).buscar(id)).withSelfRel());
        model.add(linkTo(methodOn(MetricaDiariaController.class).listar(Pageable.unpaged())).withRel("lista"));
        model.add(linkTo(methodOn(MetricaDiariaController.class).atualizar(id, this)).withRel("update"));
        model.add(linkTo(methodOn(MetricaDiariaController.class).deletar(id)).withRel("delete"));
    }
}