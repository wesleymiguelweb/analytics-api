package com.growthmachine.analytics.controller;

import com.growthmachine.analytics.model.SugestaoOtimizacao;
import com.growthmachine.analytics.service.SugestaoOtimizacaoService;
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
@RequestMapping("/api/sugestoes")
@RequiredArgsConstructor
@Tag(name = "Sugestões de Otimização", description = "Endpoints para o motor de Growth gerar e gerenciar recomendações automáticas de tráfego")
public class SugestaoOtimizacaoController {

    private final SugestaoOtimizacaoService service;
    private final PagedResourcesAssembler<SugestaoOtimizacao> assembler;

    @PostMapping
    @Operation(
            summary = "Gerar nova Sugestão de Otimização",
            description = "Cria uma recomendação estratégica e vincula a uma ou mais campanhas.\n\n**⚠️ INSTRUÇÕES DE USO:**\n* O campo `tipoAcao` é restrito. Use apenas: `AUMENTAR_ORCAMENTO`, `REDUZIR_ORCAMENTO`, `PAUSAR_CAMPANHA`, `OTIMIZAR_PALAVRAS_CHAVE` ou `AJUSTAR_PUBLICO_ALVO`.\n* As campanhas afetadas devem ser enviadas como uma **lista (Array)**. Exemplo: `\"campanhas\": [ { \"id\": 1 } ]`.\n* **NÃO** envie o campo `id` na raiz da requisição."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sugestão gerada e atrelada às campanhas com sucesso. Retorna a sugestão e os links HATEOAS."),
            @ApiResponse(responseCode = "400", description = "Erro de validação. Ocorre se o `tipoAcao` não for uma das opções válidas ou se a estrutura da lista de campanhas estiver incorreta."),
            @ApiResponse(responseCode = "500", description = "Erro de Integridade. Ocorre se algum dos IDs de campanha enviados não existir no banco de dados.")
    })
    public ResponseEntity<EntityModel<SugestaoOtimizacao>> criar(@Valid @RequestBody SugestaoOtimizacao sugestao) {
        SugestaoOtimizacao nova = service.salvar(sugestao);
        return ResponseEntity.status(HttpStatus.CREATED).body(adicionarLinks(nova));
    }

    @GetMapping
    @Operation(
            summary = "Listar todas as Sugestões",
            description = "Retorna o histórico completo de sugestões geradas. \n\n**Paginação:** Controle a exibição usando os parâmetros de URL `page` (índice da página) e `size` (itens por página)."
    )
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso. Retorna a lista envelopada com os metadados de paginação.")
    public ResponseEntity<PagedModel<EntityModel<SugestaoOtimizacao>>> listar(@PageableDefault(size = 10) Pageable pageable) {
        Page<SugestaoOtimizacao> pagina = service.listarTodos(pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, this::adicionarLinks));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar Sugestão por ID",
            description = "Recupera os detalhes de uma recomendação específica usando o seu ID."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sugestão encontrada com sucesso."),
            @ApiResponse(responseCode = "404", description = "A sugestão solicitada não foi encontrada no banco de dados.")
    })
    public ResponseEntity<EntityModel<SugestaoOtimizacao>> buscar(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(s -> ResponseEntity.ok(adicionarLinks(s)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar Sugestão Existente",
            description = "Altera o texto da descrição, o tipo de ação recomendada ou a lista de campanhas atreladas.\n\n**Nota:** O corpo da requisição deve seguir as mesmas regras do método de Criação (Enum válido e lista de campanhas)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sugestão atualizada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos novos dados fornecidos."),
            @ApiResponse(responseCode = "404", description = "O ID informado na URL não corresponde a nenhuma sugestão existente.")
    })
    public ResponseEntity<EntityModel<SugestaoOtimizacao>> atualizar(@PathVariable Long id, @Valid @RequestBody SugestaoOtimizacao atualizada) {
        return service.buscarPorId(id).map(existente -> {
            existente.setDescricao(atualizada.getDescricao());
            existente.setTipoAcao(atualizada.getTipoAcao());
            existente.setCampanhas(atualizada.getCampanhas());

            SugestaoOtimizacao salva = service.salvar(existente);
            return ResponseEntity.ok(adicionarLinks(salva));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Remover Sugestão do Histórico",
            description = "Deleta permanentemente uma sugestão de otimização.\n\n**Atenção:** Isso exclui apenas a recomendação. As campanhas atreladas a ela permanecerão intactas no banco de dados."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleção efetuada com sucesso (No Content)."),
            @ApiResponse(responseCode = "404", description = "A sugestão informada para exclusão não foi encontrada.")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (service.buscarPorId(id).isPresent()) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private EntityModel<SugestaoOtimizacao> adicionarLinks(SugestaoOtimizacao s) {
        return EntityModel.of(s,
                linkTo(methodOn(SugestaoOtimizacaoController.class).buscar(s.getId())).withSelfRel(),
                linkTo(methodOn(SugestaoOtimizacaoController.class).listar(Pageable.unpaged())).withRel("lista"),
                linkTo(methodOn(SugestaoOtimizacaoController.class).atualizar(s.getId(), s)).withRel("update"),
                linkTo(methodOn(SugestaoOtimizacaoController.class).deletar(s.getId())).withRel("delete")
        );
    }
}