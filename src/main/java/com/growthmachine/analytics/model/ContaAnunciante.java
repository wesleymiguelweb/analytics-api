package com.growthmachine.analytics.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.growthmachine.analytics.controller.ContaAnuncianteController;
import com.growthmachine.analytics.controller.HateoasLinkBuilder;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Entity
@Table(name = "tb_conta_anunciante")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidade que representa a empresa anunciante")
public class ContaAnunciante extends RepresentationModel<ContaAnunciante> implements HateoasLinkBuilder<ContaAnunciante> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da empresa é obrigatório")
    @Column(nullable = false, unique = true, length = 100)
    private String nomeEmpresa;

    @OneToMany(mappedBy = "contaAnunciante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Campanha> campanhas = new ArrayList<>();

    @Override
    public void addLinks(EntityModel<ContaAnunciante> model) {
        model.add(linkTo(methodOn(ContaAnuncianteController.class).buscar(id)).withSelfRel());
        model.add(linkTo(methodOn(ContaAnuncianteController.class).listar(Pageable.unpaged())).withRel("lista"));
        model.add(linkTo(methodOn(ContaAnuncianteController.class).atualizar(id, this)).withRel("update"));
        model.add(linkTo(methodOn(ContaAnuncianteController.class).deletar(id)).withRel("delete"));
    }
}