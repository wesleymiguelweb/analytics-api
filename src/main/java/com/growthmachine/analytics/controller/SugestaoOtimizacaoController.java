package com.growthmachine.analytics.controller;

import com.growthmachine.analytics.model.SugestaoOtimizacao;
import com.growthmachine.analytics.model.enums.TipoAcao;
import com.growthmachine.analytics.service.SugestaoOtimizacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

import java.util.Map;

@RestController
@RequestMapping("/api/sugestoes")
@RequiredArgsConstructor
@Tag(name = "Sugestões de Otimização", description = "Endpoints para o motor de Growth gerar e gerenciar recomendações automáticas de tráfego")
public class SugestaoOtimizacaoController {

    private final SugestaoOtimizacaoService service;
    private final PagedResourcesAssembler<SugestaoOtimizacao> assembler;

    @PostMapping
    @Operation(summary = "Gerar nova Sugestão de Otimização", description = "Cria uma recomendação estratégica. Este endpoint é idempotente.")
    @Parameter(in = ParameterIn.HEADER, name = "X-Idempotency-Key", description = "Chave para garantir que a operação não seja executada duas vezes.", required = false)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sugestão gerada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SugestaoOtimizacao.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<EntityModel<SugestaoOtimizacao>> criar(@Valid @RequestBody SugestaoOtimizacao sugestao) {
        SugestaoOtimizacao nova = service.salvar(sugestao);
        EntityModel<SugestaoOtimizacao> entityModel = EntityModel.of(nova);
        nova.addLinks(entityModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @GetMapping
    @Operation(summary = "Listar todas as Sugestões", description = "Retorna o histórico completo de sugestões geradas com paginação.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<PagedModel<EntityModel<SugestaoOtimizacao>>> listar(@PageableDefault(size = 10) Pageable pageable) {
        Page<SugestaoOtimizacao> pagina = service.listarTodos(pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, sugestao -> {
            EntityModel<SugestaoOtimizacao> entityModel = EntityModel.of(sugestao);
            sugestao.addLinks(entityModel);
            return entityModel;
        }));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Sugestão por ID", description = "Recupera os detalhes de uma recomendação específica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sugestão encontrada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SugestaoOtimizacao.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "A sugestão solicitada não foi encontrada.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<EntityModel<SugestaoOtimizacao>> buscar(@PathVariable Long id) {
        SugestaoOtimizacao sugestao = service.buscarPorId(id);
        EntityModel<SugestaoOtimizacao> entityModel = EntityModel.of(sugestao);
        sugestao.addLinks(entityModel);
        return ResponseEntity.ok(entityModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Sugestão Existente", description = "Altera o texto da descrição, o tipo de ação ou as campanhas atreladas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sugestão atualizada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = SugestaoOtimizacao.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos novos dados fornecidos.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "A sugestão informada não existe.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<EntityModel<SugestaoOtimizacao>> atualizar(@PathVariable Long id, @Valid @RequestBody SugestaoOtimizacao atualizada) {
        SugestaoOtimizacao salva = service.atualizar(id, atualizada);
        EntityModel<SugestaoOtimizacao> entityModel = EntityModel.of(salva);
        salva.addLinks(entityModel);
        return ResponseEntity.ok(entityModel);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover Sugestão do Histórico", description = "Deleta permanentemente uma sugestão de otimização.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleção efetuada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "A sugestão informada não foi encontrada."),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/busca-por-tipo")
    @Operation(summary = "Filtrar por Tipo de Ação", description = "Busca sugestões específicas baseadas no tipo de recomendação (ex: AUMENTAR_ORCAMENTO).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<PagedModel<EntityModel<SugestaoOtimizacao>>> buscarPorTipo(
            @RequestParam TipoAcao tipoAcao,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<SugestaoOtimizacao> pagina = service.findByTipoAcao(tipoAcao, pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, sugestao -> {
            EntityModel<SugestaoOtimizacao> entityModel = EntityModel.of(sugestao);
            sugestao.addLinks(entityModel);
            return entityModel;
        }));
    }
}