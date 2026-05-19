package com.growthmachine.analytics.controller;

import com.growthmachine.analytics.model.ContaAnunciante;
import com.growthmachine.analytics.service.ContaAnuncianteService;
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

import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/contas")
@RequiredArgsConstructor
@Tag(name = "Contas Anunciantes", description = "Entidade RAIZ do sistema. Endpoints para gestão de empresas e anunciantes.")
public class ContaAnuncianteController {

    private final ContaAnuncianteService service;
    private final PagedResourcesAssembler<ContaAnunciante> assembler;

    @PostMapping
    @Operation(
            summary = "Criar nova Conta Anunciante",
            description = "Cadastra uma nova empresa no sistema. Sendo a entidade raiz, não depende de nenhuma outra para ser criada.\n\n**⚠️ INSTRUÇÕES DE USO:**\n* Envie apenas as informações da empresa no JSON, como o `nomeEmpresa`.\n* **NÃO** envie o campo `id` na requisição. O banco de dados gerará a numeração automaticamente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Conta criada com sucesso. Retorna os dados cadastrados e os links HATEOAS."),
            @ApiResponse(responseCode = "400", description = "Erro de validação. Ocorre se o nome da empresa for enviado em branco ou fora do formato permitido.")
    })
    public ResponseEntity<EntityModel<ContaAnunciante>> criar(@Valid @RequestBody ContaAnunciante conta) {
        ContaAnunciante novaConta = service.salvar(conta);
        return ResponseEntity.status(HttpStatus.CREATED).body(adicionarLinksHateoas(novaConta));
    }

    @GetMapping
    @Operation(
            summary = "Listar todas as Contas",
            description = "Retorna o diretório completo de empresas anunciantes cadastradas.\n\n**Paginação:** Controle a navegação utilizando os parâmetros `page` (página inicial é 0) e `size` (limite de itens) na URL."
    )
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso. Retorna a lista paginada com links de navegação.")
    public ResponseEntity<PagedModel<EntityModel<ContaAnunciante>>> listarTodos(@PageableDefault(size = 10) Pageable pageable) {
        Page<ContaAnunciante> contas = service.listarTodos(pageable);
        PagedModel<EntityModel<ContaAnunciante>> pagedModel = assembler.toModel(contas, this::adicionarLinksHateoas);
        return ResponseEntity.ok(pagedModel);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar Conta por ID",
            description = "Consulta os dados cadastrais completos de uma empresa específica utilizando o seu ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta encontrada com sucesso."),
            @ApiResponse(responseCode = "404", description = "O ID informado não corresponde a nenhuma conta no banco de dados.")
    })
    public ResponseEntity<EntityModel<ContaAnunciante>> buscarPorId(@PathVariable Long id) {
        Optional<ContaAnunciante> conta = service.buscarPorId(id);
        return conta.map(c -> ResponseEntity.ok(adicionarLinksHateoas(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar Conta Existente",
            description = "Modifica os dados cadastrais (como alterar o nome da empresa) de uma conta existente."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Conta atualizada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos novos dados enviados."),
            @ApiResponse(responseCode = "404", description = "O ID informado na URL não foi encontrado para atualização.")
    })
    public ResponseEntity<EntityModel<ContaAnunciante>> atualizar(@PathVariable Long id, @Valid @RequestBody ContaAnunciante contaAtualizada) {
        return service.buscarPorId(id).map(contaExistente -> {
            contaExistente.setNomeEmpresa(contaAtualizada.getNomeEmpresa());
            ContaAnunciante salva = service.salvar(contaExistente);
            return ResponseEntity.ok(adicionarLinksHateoas(salva));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deletar Conta (Perigo)",
            description = "Exclui uma empresa anunciante do sistema.\n\n**⚠️ ALERTA DE INTEGRIDADE (EFEITO CASCATA):**\nDependendo de como a restrição de chave estrangeira foi configurada no banco de dados, deletar uma Conta pode falhar se ela possuir Campanhas e Metas vinculadas a ela, ou apagará tudo em cascata."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Conta deletada com sucesso (No Content)."),
            @ApiResponse(responseCode = "404", description = "A conta informada para exclusão não foi encontrada."),
            @ApiResponse(responseCode = "500", description = "Erro de Integridade. A conta não pode ser apagada pois possui Campanhas ativas vinculadas (Violação de Chave Estrangeira).")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (service.buscarPorId(id).isPresent()) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private EntityModel<ContaAnunciante> adicionarLinksHateoas(ContaAnunciante conta) {
        return EntityModel.of(conta,
                linkTo(methodOn(ContaAnuncianteController.class).buscarPorId(conta.getId())).withSelfRel(),
                linkTo(methodOn(ContaAnuncianteController.class).listarTodos(Pageable.unpaged())).withRel("todas_contas"),
                linkTo(methodOn(ContaAnuncianteController.class).atualizar(conta.getId(), conta)).withRel("update"),
                linkTo(methodOn(ContaAnuncianteController.class).deletar(conta.getId())).withRel("delete")
        );
    }
}