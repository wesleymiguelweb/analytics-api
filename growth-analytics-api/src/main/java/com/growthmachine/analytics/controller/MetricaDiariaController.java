package com.growthmachine.analytics.controller;

import com.growthmachine.analytics.model.MetricaDiaria;
import com.growthmachine.analytics.service.MetricaDiariaService;
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
@RequestMapping("/api/metricas")
@RequiredArgsConstructor
@Tag(name = "Métricas Diárias", description = "Endpoints para registro de dados brutos e cálculo automático de performance (Motor de Growth)")
public class MetricaDiariaController {

    private final MetricaDiariaService service;
    private final PagedResourcesAssembler<MetricaDiaria> assembler;

    @PostMapping
    @Operation(
            summary = "Registrar Novas Métricas Diárias",
            description = "Cadastra os dados brutos de performance de um dia específico.\n\n**⚠️ INSTRUÇÕES DE EXECUÇÃO IMPORTANTES:**\n* **NÃO ENVIE** os campos `roas`, `cpa` e `ctr`. A inteligência do sistema calcula esses indicadores automaticamente com base nos dados brutos.\n* É **obrigatório** vincular esta métrica a uma Campanha existente. Adicione o bloco `\"campanha\": { \"id\": X }` no final do JSON.\n* O formato do campo `data` deve ser `YYYY-MM-DD`.\n* **NÃO** envie o campo `id` na raiz da requisição."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Métricas registradas com sucesso. A resposta retornará o JSON completo com os indicadores (ROAS, CPA, CTR) já calculados."),
            @ApiResponse(responseCode = "400", description = "Erro de validação de Sintaxe. Ocorre se tentar enviar campos restritos (read-only) ou esquecer dados obrigatórios (ex: cliques, custo)."),
            @ApiResponse(responseCode = "500", description = "Erro de Integridade. Ocorre se o ID da Campanha informado não existir no banco de dados.")
    })
    public ResponseEntity<EntityModel<MetricaDiaria>> criar(@Valid @RequestBody MetricaDiaria metrica) {
        MetricaDiaria novaMetrica = service.salvar(metrica);
        return ResponseEntity.status(HttpStatus.CREATED).body(adicionarLinksHateoas(novaMetrica));
    }

    @GetMapping
    @Operation(
            summary = "Listar todas as Métricas",
            description = "Retorna o histórico completo de métricas diárias registradas no sistema. \n\n**Paginação:** Você pode refinar a busca usando os parâmetros `page` (índice da página) e `size` (quantidade de registros por página) na URL."
    )
    @ApiResponse(responseCode = "200", description = "Busca realizada com sucesso. Retorna a lista envelopada com metadados de paginação.")
    public ResponseEntity<PagedModel<EntityModel<MetricaDiaria>>> listarTodos(@PageableDefault(size = 10) Pageable pageable) {
        Page<MetricaDiaria> metricas = service.listarTodos(pageable);
        return ResponseEntity.ok(assembler.toModel(metricas, this::adicionarLinksHateoas));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar Métrica por ID",
            description = "Recupera o extrato detalhado de performance de um dia específico usando o seu ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métrica encontrada com sucesso."),
            @ApiResponse(responseCode = "404", description = "O ID da métrica informada não existe no banco de dados.")
    })
    public ResponseEntity<EntityModel<MetricaDiaria>> buscarPorId(@PathVariable Long id) {
        return service.buscarPorId(id)
                .map(m -> ResponseEntity.ok(adicionarLinksHateoas(m)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Atualizar Métricas Existentes",
            description = "Sobrescreve os dados brutos de um registro diário existente.\n\n**⚠️ ATENÇÃO:** Assim como na criação, **não envie** os campos calculados (`roas`, `cpa`, `ctr`). O sistema fará o recálculo automático com base nos novos dados enviados."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Métrica atualizada e recalculada com sucesso."),
            @ApiResponse(responseCode = "404", description = "A métrica informada na URL para atualização não foi encontrada."),
            @ApiResponse(responseCode = "400", description = "Erro de validação nos novos dados fornecidos.")
    })
    public ResponseEntity<EntityModel<MetricaDiaria>> atualizar(@PathVariable Long id, @Valid @RequestBody MetricaDiaria metricaAtualizada) {
        return service.buscarPorId(id).map(metricaExistente -> {
            metricaExistente.setData(metricaAtualizada.getData());
            metricaExistente.setCliques(metricaAtualizada.getCliques());
            metricaExistente.setImpressoes(metricaAtualizada.getImpressoes());
            metricaExistente.setCusto(metricaAtualizada.getCusto());
            metricaExistente.setConversoes(metricaAtualizada.getConversoes());
            metricaExistente.setGmv(metricaAtualizada.getGmv());
            metricaExistente.setCampanha(metricaAtualizada.getCampanha());

            MetricaDiaria salva = service.salvar(metricaExistente);
            return ResponseEntity.ok(adicionarLinksHateoas(salva));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Deletar Métrica",
            description = "Remove permanentemente um registro de métrica diária do histórico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Registro deletado com sucesso (No Content)."),
            @ApiResponse(responseCode = "404", description = "A métrica informada para exclusão não foi encontrada.")
    })
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        if (service.buscarPorId(id).isPresent()) {
            service.deletar(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    private EntityModel<MetricaDiaria> adicionarLinksHateoas(MetricaDiaria metrica) {
        return EntityModel.of(metrica,
                linkTo(methodOn(MetricaDiariaController.class).buscarPorId(metrica.getId())).withSelfRel(),
                linkTo(methodOn(MetricaDiariaController.class).listarTodos(Pageable.unpaged())).withRel("todas_metricas"),
                linkTo(methodOn(MetricaDiariaController.class).atualizar(metrica.getId(), metrica)).withRel("update"),
                linkTo(methodOn(MetricaDiariaController.class).deletar(metrica.getId())).withRel("delete")
        );
    }
}