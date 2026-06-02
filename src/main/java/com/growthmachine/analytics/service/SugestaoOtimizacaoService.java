package com.growthmachine.analytics.service;

import com.growthmachine.analytics.exception.ResourceNotFoundException;
import com.growthmachine.analytics.model.SugestaoOtimizacao;
import com.growthmachine.analytics.model.enums.TipoAcao;
import com.growthmachine.analytics.repository.SugestaoOtimizacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SugestaoOtimizacaoService {

    private final SugestaoOtimizacaoRepository repository;

    public Page<SugestaoOtimizacao> listarTodos(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public SugestaoOtimizacao buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sugestão de Otimização não encontrada com o ID: " + id));
    }

    public Page<SugestaoOtimizacao> findByTipoAcao(TipoAcao tipoAcao, Pageable pageable) {
        return repository.findByTipoAcao(tipoAcao, pageable);
    }

    public SugestaoOtimizacao salvar(SugestaoOtimizacao sugestao) {
        return repository.save(sugestao);
    }

    public SugestaoOtimizacao atualizar(Long id, SugestaoOtimizacao sugestaoAtualizada) {
        SugestaoOtimizacao existente = buscarPorId(id);
        existente.setDescricao(sugestaoAtualizada.getDescricao());
        existente.setTipoAcao(sugestaoAtualizada.getTipoAcao());
        existente.setCampanhas(sugestaoAtualizada.getCampanhas());
        return repository.save(existente);
    }

    public void deletar(Long id) {
        buscarPorId(id);
        repository.deleteById(id);
    }
}