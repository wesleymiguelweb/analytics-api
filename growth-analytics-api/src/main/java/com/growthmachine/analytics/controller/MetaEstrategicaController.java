package com.growthmachine.analytics.controller;

import com.growthmachine.analytics.model.MetaEstrategica;
import com.growthmachine.analytics.service.MetaEstrategicaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/metas")
@RequiredArgsConstructor
@Tag(name = "Metas Estratégicas", description = "Endpoints para gestão de orçamentos e ROAS das contas")
public class MetaEstrategicaController {

    private final MetaEstrategicaService service;
    private final PagedResourcesAssembler<MetaEstrategica> assembler;

    @PostMapping
    @Operation(
            summary = "Definir Meta Estratégica",
            description = "Estabelece os limites financeiros e de performance para uma conta anunciante.\n\n**⚠️ INSTRUÇÕES DE USO:**\n* A relação no banco é de 1 para 1 (`@OneToOne`). Cada conta só pode ter **UMA** meta ativa.\n* **NÃO** envie o campo `id` na raiz do JSON (ele é gerado automaticamente).\n* É **obrigatório** vincular a meta a uma Conta Anunciante existente enviando: `\"contaAnunciante\": { \"id\": X }` no corpo da requisição."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Meta criada com sucesso. Retorna os dados da meta e os links de navegação."),
            @ApiResponse(responseCode = "400", description = "Erro de validação. Ocorre se enviar orçamento/ROAS negativos ou esquecer campos obrigatórios."),
            @ApiResponse(responseCode = "500", description = "Erro de Integridade. Ocorre se o ID da Conta não existir ou se a Conta já possuir uma meta vinculada (violação da regra 1 para 1).")
    })
    public ResponseEntity<EntityModel<MetaEstrategica>> criar(@Valid @RequestBody MetaEstrategica meta) {
        MetaEstrategica novaMeta = service.salvar(meta);
        return ResponseEntity.status(HttpStatus.CREATED).body(adicionarLinksHateoas(novaMeta));
    }

    @GetMapping
    @Operation(
            summary = "Listar todas as Metas",
            description = "Retorna uma lista com todas as metas cadastradas no sistema. \n\n**Paginação:** Esta rota é paginada. Você pode controlar o retorno usando os parâmetros `page` (página desejada, começando em 0) e `size` (quantidade por página) na URL."
    )
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso. Retorna a lista envelopada com metadados de paginação.")
    public ResponseEntity<PagedModel<EntityModel<MetaEstrategica>>> listarTodos(@PageableDefault(size = 10) Pageable pageable) {
        Page<MetaEstrategica> metas = service.listarTodos(pageable);
        return ResponseEntity.ok(assembler.toModel(metas, this::adicionarLinksHateoas));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar Meta por ID",
            description = "Recupera os detalhes de uma meta estratégica específica baseada no ID informado na URL."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meta encontrada com sucesso."),
            @ApiResponse(responseCode = "404", description = "A meta solicitada não existe no banco de dados.")
    })
    public ResponseEntity<EntityModel<MetaEstrategica>> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(m -> ResponseEntity.ok(adicionarLinksHateoas(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar Meta Existente",
            description = "Sobrescreve os valores de uma meta existente. \n\n**Nota:** O ID passado na URL deve ser de uma meta existente. O corpo (JSON) deve conter os novos dados de orçamento e ROAS."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meta atualizada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos novos dados enviados."),
            @ApiResponse(responseCode = "404", description = "A meta informada para atualização não foi encontrada.")
    })
    public ResponseEntity<EntityModel<MetaEstrategica>> atualizar(@PathVariable Long id, @Valid @RequestBody MetaEstrategica metaAtualizada) {
        return service.buscarPorId(id).map(metaExistente -> {
            metaExistente.setOrcamentoMensal(metaAtualizada.getOrcamentoMensal());
            metaExistente.setRoasAlvo(metaAtualizada.getRoasAlvo());
            metaExistente.setContaAnunciante(metaAtualizada.getContaAnunciante());

            MetaEstrategica salva = service.salvar(metaExistente);
            return ResponseEntity.ok(adicionarLinksHateoas(salva));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deletar Meta",
            description = "Remove uma meta estratégica do sistema. \n\n**Atenção:** Esta ação apaga apenas a meta, não afeta a Conta Anunciante vinculada a ela."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Meta deletada com sucesso (No Content)."),
            @ApiResponse(responseCode = "404", description = "A meta informada não foi encontrada para exclusão.")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (service.buscarPorId(id).isPresent()) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private EntityModel<MetaEstrategica> adicionarLinksHateoas(MetaEstrategica meta) {
        return EntityModel.of(meta,
                linkTo(methodOn(MetaEstrategicaController.class).buscarPorId(meta.getId())).withSelfRel(),
                linkTo(methodOn(MetaEstrategicaController.class).listarTodos(Pageable.unpaged())).withRel("todas_metas"),
                linkTo(methodOn(MetaEstrategicaController.class).atualizar(meta.getId(), meta)).withRel("update"),
                linkTo(methodOn(MetaEstrategicaController.class).deletar(meta.getId())).withRel("delete")
        );
    }
}