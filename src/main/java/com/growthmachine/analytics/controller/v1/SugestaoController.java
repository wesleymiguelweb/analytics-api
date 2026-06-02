package com.growthmachine.analytics.controller.v1;

import com.growthmachine.analytics.model.SugestaoOtimizacao;
import com.growthmachine.analytics.repository.SugestaoOtimizacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController("sugestaoControllerV1")
@RequestMapping("/api/v1/sugestoes")
public class SugestaoController {

    @Autowired
    private SugestaoOtimizacaoRepository repository;

    @GetMapping
    public Page<SugestaoOtimizacao> listar(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @GetMapping("/{id}")
    public EntityModel<SugestaoOtimizacao> buscar(@PathVariable Long id) {
        SugestaoOtimizacao sugestao = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sugestão não encontrada com o ID: " + id));

        // Construímos o EntityModel com o link
        return EntityModel.of(sugestao,
                linkTo(methodOn(SugestaoController.class).buscar(id)).withSelfRel(),
                linkTo(methodOn(SugestaoController.class).listar(null)).withRel("sugestoes"));
    }
}