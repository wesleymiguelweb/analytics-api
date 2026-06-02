package com.growthmachine.analytics.infrastructure.adapter.in.web.controller.v2;

import com.growthmachine.analytics.domain.model.SugestaoOtimizacao;
import com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository.SugestaoOtimizacaoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController("sugestaoControllerV2")
@RequestMapping(value = "/api/sugestoes-demo", headers = "X-API-Version=2")
@RequiredArgsConstructor
@Tag(name = "Sugestões Demo v2", description = "Endpoint demonstrativo de versionamento por header X-API-Version=2")
public class SugestaoController {

    private final SugestaoOtimizacaoRepository repository;

    @GetMapping("/destaque")
    @Operation(summary = "Listar sugestões em destaque demo v2", description = "Retorna sugestões no formato da versão 2. Use o header X-API-Version=2.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Listagem paginada realizada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Parâmetros de paginação inválidos."),
            @ApiResponse(responseCode = "401", description = "X-API-Key ausente ou inválida."),
            @ApiResponse(responseCode = "429", description = "Limite de requisições excedido."),
            @ApiResponse(responseCode = "500", description = "Erro inesperado no servidor.")
    })
    public Page<SugestaoOtimizacao> listarDestaques(Pageable pageable) {
        return repository.findAll(pageable);
    }
}
