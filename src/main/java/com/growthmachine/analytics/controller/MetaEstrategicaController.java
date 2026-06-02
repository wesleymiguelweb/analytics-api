package com.growthmachine.analytics.controller;

import com.growthmachine.analytics.model.MetaEstrategica;
import com.growthmachine.analytics.service.MetaEstrategicaService;
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

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/metas")
@RequiredArgsConstructor
@Tag(name = "Metas Estratégicas", description = "Endpoints para definição de orçamento e ROAS alvo por Anunciante")
public class MetaEstrategicaController {

    private final MetaEstrategicaService service;
    private final PagedResourcesAssembler<MetaEstrategica> assembler;

    @PostMapping
    @Operation(summary = "Criar nova Meta", description = "Define o orçamento mensal e o ROAS alvo para uma Conta Anunciante. Este endpoint é idempotente.")
    @Parameter(in = ParameterIn.HEADER, name = "X-Idempotency-Key", description = "Chave para garantir que a operação não seja executada duas vezes.", required = false)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Meta criada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetaEstrategica.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<EntityModel<MetaEstrategica>> criar(@Valid @RequestBody MetaEstrategica meta) {
        MetaEstrategica nova = service.salvar(meta);
        EntityModel<MetaEstrategica> entityModel = EntityModel.of(nova);
        nova.addLinks(entityModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
    }

    @GetMapping
    @Operation(summary = "Listar todas as Metas", description = "Retorna uma lista paginada com as diretrizes estratégicas ativas.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<PagedModel<EntityModel<MetaEstrategica>>> listar(@PageableDefault(size = 10) Pageable pageable) {
        Page<MetaEstrategica> pagina = service.listarTodos(pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, meta -> {
            EntityModel<MetaEstrategica> entityModel = EntityModel.of(meta);
            meta.addLinks(entityModel);
            return entityModel;
        }));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Meta por ID", description = "Recupera os detalhes de uma meta estratégica específica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Meta encontrada.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetaEstrategica.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Meta não encontrada no banco de dados.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<EntityModel<MetaEstrategica>> buscar(@PathVariable Long id) {
        MetaEstrategica meta = service.buscarPorId(id);
        EntityModel<MetaEstrategica> entityModel = EntityModel.of(meta);
        meta.addLinks(entityModel);
        return ResponseEntity.ok(entityModel);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Meta Estratégica", description = "Altera os valores de orçamento ou ROAS alvo de uma meta existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Meta atualizada com sucesso.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetaEstrategica.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos novos dados enviados.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Meta não encontrada para atualização.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<EntityModel<MetaEstrategica>> atualizar(@PathVariable Long id, @Valid @RequestBody MetaEstrategica atualizada) {
        MetaEstrategica salva = service.atualizar(id, atualizada);
        EntityModel<MetaEstrategica> entityModel = EntityModel.of(salva);
        salva.addLinks(entityModel);
        return ResponseEntity.ok(entityModel);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Excluir Meta", description = "Remove uma meta estratégica do sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Exclusão efetuada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "404", description = "Meta não encontrada.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/busca-por-roas")
    @Operation(summary = "Filtrar por ROAS Mínimo", description = "Busca metas que possuam um ROAS alvo maior ou igual ao valor informado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso."),
            @ApiResponse(responseCode = "403", description = "Acesso negado. Chave de API (X-API-Key) inválida ou ausente.", content = @Content),
            @ApiResponse(responseCode = "429", description = "Excesso de requisições. Tente novamente mais tarde.", content = @Content),
            @ApiResponse(responseCode = "500", description = "Ocorreu um erro inesperado no servidor.", content = @Content)
    })
    public ResponseEntity<PagedModel<EntityModel<MetaEstrategica>>> buscarPorRoasMinimo(
            @RequestParam BigDecimal roas,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MetaEstrategica> pagina = service.buscarPorRoasMinimo(roas, pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, meta -> {
            EntityModel<MetaEstrategica> entityModel = EntityModel.of(meta);
            meta.addLinks(entityModel);
            return entityModel;
        }));
    }
}