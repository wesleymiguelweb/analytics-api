package com.growthmachine.analytics.model;

import com.growthmachine.analytics.controller.HateoasLinkBuilder;
import com.growthmachine.analytics.controller.MetaEstrategicaController;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Entity
@Table(name = "tb_meta_estrategica")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidade que representa as metas financeiras e de performance de uma conta")
public class MetaEstrategica extends RepresentationModel<MetaEstrategica> implements HateoasLinkBuilder<MetaEstrategica> {

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

    @OneToOne
    @JoinColumn(name = "conta_anunciante_id", referencedColumnName = "id", unique = true, nullable = false)
    @Schema(description = "A conta anunciante dona desta meta")
    private ContaAnunciante contaAnunciante;

    @Override
    public void addLinks(EntityModel<MetaEstrategica> model) {
        model.add(linkTo(methodOn(MetaEstrategicaController.class).buscar(id)).withSelfRel());
        model.add(linkTo(methodOn(MetaEstrategicaController.class).listar(Pageable.unpaged())).withRel("lista"));
        model.add(linkTo(methodOn(MetaEstrategicaController.class).atualizar(id, this)).withRel("update"));
        model.add(linkTo(methodOn(MetaEstrategicaController.class).deletar(id)).withRel("delete"));
    }
}