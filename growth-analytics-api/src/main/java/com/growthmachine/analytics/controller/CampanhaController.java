package com.growthmachine.analytics.controller;

import com.growthmachine.analytics.model.Campanha;
import com.growthmachine.analytics.service.CampanhaService;
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
@RequestMapping("/api/campanhas")
@RequiredArgsConstructor
@Tag(name = "Campanhas", description = "Endpoints para gestão das campanhas de tráfego pago")
public class CampanhaController {

    private final CampanhaService service;
    private final PagedResourcesAssembler<Campanha> assembler;

    @PostMapping
    @Operation(
            summary = "Criar nova Campanha",
            description = "Cadastra uma nova campanha de tráfego pago associada a uma conta.\n\n**⚠️ INSTRUÇÕES:**\n* O campo `canalOrigem` só aceita valores exatos: `GOOGLE_ADS`, `MERCADO_LIVRE`, `AMAZON_ADS` ou `META_ADS`.\n* É **obrigatório** vincular a campanha a uma Conta Anunciante existente enviando o bloco `contaAnunciante: { \"id\": X }` no corpo do JSON."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Campanha criada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Erro de validação. Ocorre se o Enum do canal de origem estiver escrito errado ou faltar o nome da campanha."),
            @ApiResponse(responseCode = "500", description = "Erro de Integridade. Ocorre se o ID da Conta Anunciante enviada não for encontrado no banco de dados.")
    })
    public ResponseEntity<Campanha> criar(@Valid @RequestBody Campanha campanha) { ... }

    @GetMapping
    @Operation(
            summary = "Listar Campanhas",
            description = "Retorna uma lista de todas as campanhas cadastradas. Suporta paginação passando os parâmetros `page` e `size`."
    )
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso.")
    public ResponseEntity<Page<Campanha>> listarTodas(Pageable pageable) { ... }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar campanha por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campanha encontrada"),
            @ApiResponse(responseCode = "404", description = "Campanha não encontrada")
    })
    public ResponseEntity<EntityModel<Campanha>> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(c -> ResponseEntity.ok(adicionarLinksHateoas(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar uma campanha existente")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Campanha atualizada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Campanha não encontrada"),
            @ApiResponse(responseCode = "400", description = "Erro de validação")
    })
    public ResponseEntity<EntityModel<Campanha>> atualizar(@PathVariable Long id, @Valid @RequestBody Campanha campanhaAtualizada) {
        return service.buscarPorId(id).map(campanhaExistente -> {
            campanhaExistente.setNomeCampanha(campanhaAtualizada.getNomeCampanha());
            campanhaExistente.setCanalOrigem(campanhaAtualizada.getCanalOrigem());
            campanhaExistente.setContaAnunciante(campanhaAtualizada.getContaAnunciante());

            Campanha salva = service.salvar(campanhaExistente);
            return ResponseEntity.ok(adicionarLinksHateoas(salva));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar uma campanha por ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Campanha deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Campanha não encontrada")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (service.buscarPorId(id).isPresent()) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private EntityModel<Campanha> adicionarLinksHateoas(Campanha campanha) {
        return EntityModel.of(campanha,
                linkTo(methodOn(CampanhaController.class).buscarPorId(campanha.getId())).withSelfRel(),
                linkTo(methodOn(CampanhaController.class).listarTodos(Pageable.unpaged())).withRel("todas_campanhas"),
                linkTo(methodOn(CampanhaController.class).atualizar(campanha.getId(), campanha)).withRel("update"),
                linkTo(methodOn(CampanhaController.class).deletar(campanha.getId())).withRel("delete")
        );
    }
}