package com.growthmachine.analytics.infrastructure.adapter.in.web.controller.v1;

import com.growthmachine.analytics.domain.model.SugestaoOtimizacao;
import com.growthmachine.analytics.domain.exception.ResourceNotFoundException;
import com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository.SugestaoOtimizacaoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.web.bind.annotation.*;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController("sugestaoControllerV1")
@RequestMapping(value = "/api/sugestoes-demo", headers = "X-API-Version=1")
@Tag(name = "Sugestões Demo v1", description = "Endpoint demonstrativo de versionamento por header X-API-Version=1")
public class SugestaoController {

    @Autowired
    private SugestaoOtimizacaoRepository repository;

    @GetMapping
    @Operation(summary = "Listar sugestões demo v1", description = "Retorna sugestões no formato da versão 1. Use o header X-API-Version=1.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem paginada realizada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos."),
            @ApiResponse(responseCode = "401", description = "X-API-Key ausente ou inválida."),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido."),
            @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor.")
    })
    public Page<SugestaoOtimizacao> listar(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar sugestão demo v1 por ID", description = "Busca uma sugestão no formato da versão 1. Use o header X-API-Version=1.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sugestão encontrada com sucesso."),
            @ApiResponse(responseCode = "400", description = "ID inválido."),
            @ApiResponse(responseCode = "401", description = "X-API-Key ausente ou inválida."),
            @ApiResponse(responseCode = "404", description = "Sugestão não encontrada."),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido."),
            @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor.")
    })
    public EntityModel<SugestaoOtimizacao> buscar(@PathVariable Long id) {
        SugestaoOtimizacao sugestao = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sugestão não encontrada com o ID: " + id));

        return EntityModel.of(sugestao,
                linkTo(methodOn(SugestaoController.class).buscar(id)).withSelfRel(),
                linkTo(methodOn(SugestaoController.class).listar(null)).withRel("sugestoes"));
    }
}
