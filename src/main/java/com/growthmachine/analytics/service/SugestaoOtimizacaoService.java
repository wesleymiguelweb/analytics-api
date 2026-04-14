package com.growthmachine.analytics.service;

import com.growthmachine.analytics.model.SugestaoOtimizacao;
import com.growthmachine.analytics.repository.SugestaoOtimizacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SugestaoOtimizacaoService {

    private final SugestaoOtimizacaoRepository repository;

    public SugestaoOtimizacao salvar(SugestaoOtimizacao sugestao) {
        return repository.save(sugestao);
    }

    public Page<SugestaoOtimizacao> listarTodos(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Optional<SugestaoOtimizacao> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }
}