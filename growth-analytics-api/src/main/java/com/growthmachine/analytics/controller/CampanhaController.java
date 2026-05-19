package com.growthmachine.analytics.controller;

import com.growthmachine.analytics.exception.ErroResposta;
import com.growthmachine.analytics.model.Campanha;
import com.growthmachine.analytics.model.enums.CanalOrigem;
import com.growthmachine.analytics.service.CampanhaService;
import io.swagger.v3.oas.annotations.Operation;
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

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping("/api/campanhas")
@RequiredArgsConstructor
@Tag(name = "Campanhas", description = "Endpoints para gestão de campanhas de tráfego pago")
public class CampanhaController {

    private final CampanhaService service;
    private final PagedResourcesAssembler<Campanha> assembler;

    @PostMapping
    @Operation(summary = "Criar nova Campanha", description = "Registra uma nova campanha vinculada a uma Conta Anunciante.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Campanha criada com sucesso.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Campanha.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação (Ex: nome em branco ou ID do anunciante ausente).",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResposta.class)))
    })
    public ResponseEntity<EntityModel<Campanha>> criar(@Valid @RequestBody Campanha campanha) {
        Campanha nova = service.salvar(campanha);
        return ResponseEntity.status(HttpStatus.CREATED).body(adicionarLinks(nova));
    }

    @GetMapping
    @Operation(summary = "Listar todas as Campanhas", description = "Retorna uma lista paginada de todas as campanhas cadastradas.")
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso.")
    public ResponseEntity<PagedModel<EntityModel<Campanha>>> listar(@PageableDefault(size = 10) Pageable pageable) {
        Page<Campanha> pagina = service.listarTodos(pageable);
        return ResponseEntity.ok(assembler.toModel(pagina, this::adicionarLinks));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar Campanha por ID", description = "Recupera os detalhes de uma campanha específica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Campanha encontrada.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Campanha.class))),
            @ApiResponse(responseCode = "404", description = "Campanha não encontrada no banco de dados.",
                    content = @Content(schema = @Schema(hidden = true)))
    })
    public ResponseEntity<EntityModel<Campanha>> buscar(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(c -> ResponseEntity.ok(adicionarLinks(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar Campanha", description = "Altera os dados de uma campanha existente.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Campanha atualizada com sucesso.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = Campanha.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos novos dados enviados.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErroResposta.class))),
            @ApiResponse(responseCode = "404", description = "Campanha não encontrada