package com.growthmachine.analytics.controller.v2;

import com.growthmachine.analytics.model.SugestaoOtimizacao;
import com.growthmachine.analytics.repository.SugestaoOtimizacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("sugestaoControllerV2")
@RequestMapping("/api/v2/sugestoes")
public class SugestaoController {

    @Autowired
    private SugestaoOtimizacaoRepository repository;

    // Exemplo de endpoint de consulta personalizada exclusivo da v2
    @GetMapping("/destaque")
    public List<SugestaoOtimizacao> listarDestaques() {
        return repository.findAll(); // Aqui entraria uma query customizada (ex: pelo maior ROAS)
    }
}