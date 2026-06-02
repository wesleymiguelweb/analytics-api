package com.growthmachine.analytics.model;

import com.growthmachine.analytics.controller.CampanhaController;
import com.growthmachine.analytics.controller.HateoasLinkBuilder;
import com.growthmachine.analytics.model.enums.CanalOrigem;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Entity
@Table(name = "tb_campanha")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidade que representa uma campanha de tráfego pago")
public class Campanha extends RepresentationModel<Campanha> implements HateoasLinkBuilder<Campanha> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nomeCampanha;

    @NotNull(message = "O canal de origem é obrigatório")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Schema(description = "Plataforma onde a campanha está rodando")
    private CanalOrigem canalOrigem;

    @NotNull(message = "A conta anunciante é obrigatória")
    @ManyToOne
    @JoinColumn(name = "conta_anunciante_id", nullable = false)
    @Schema(description = "A conta anunciante dona desta campanha")
    private ContaAnunciante contaAnunciante;

    @Override
    public void addLinks(EntityModel<Campanha> model) {
        model.add(linkTo(methodOn(CampanhaController.class).buscar(id)).withSelfRel());
        model.add(linkTo(methodOn(CampanhaController.class).listar(Pageable.unpaged())).withRel("lista"));
        // Não há método de atualização (PUT) neste controller, então o link não é adicionado.
        model.add(linkTo(methodOn(CampanhaController.class).deletar(id)).withRel("delete"));
    }
}