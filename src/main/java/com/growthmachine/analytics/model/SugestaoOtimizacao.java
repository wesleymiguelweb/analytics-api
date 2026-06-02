package com.growthmachine.analytics.model;

import com.growthmachine.analytics.controller.HateoasLinkBuilder;
import com.growthmachine.analytics.controller.SugestaoOtimizacaoController;
import com.growthmachine.analytics.model.enums.TipoAcao;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.RepresentationModel;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Entity
@Table(name = "tb_sugestao_otimizacao")
@Data
public class SugestaoOtimizacao extends RepresentationModel<SugestaoOtimizacao> implements HateoasLinkBuilder<SugestaoOtimizacao> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descricao;

    @Enumerated(EnumType.STRING)
    private TipoAcao tipoAcao;

    @ManyToMany
    @JoinTable(
            name = "tb_sugestao_campanha",
            joinColumns = @JoinColumn(name = "sugestao_id"),
            inverseJoinColumns = @JoinColumn(name = "campanha_id")
    )
    private List<Campanha> campanhas = new ArrayList<>();

    @Override
    public void addLinks(EntityModel<SugestaoOtimizacao> model) {
        model.add(linkTo(methodOn(SugestaoOtimizacaoController.class).buscar(id)).withSelfRel());
        model.add(linkTo(methodOn(SugestaoOtimizacaoController.class).listar(Pageable.unpaged())).withRel("lista"));
        model.add(linkTo(methodOn(SugestaoOtimizacaoController.class).atualizar(id, this)).withRel("update"));
        model.add(linkTo(methodOn(SugestaoOtimizacaoController.class).deletar(id)).withRel("delete"));
    }
}