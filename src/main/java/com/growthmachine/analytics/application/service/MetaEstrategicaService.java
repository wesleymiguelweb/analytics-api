package com.growthmachine.analytics.application.service;

import com.growthmachine.analytics.domain.exception.ResourceNotFoundException;
import com.growthmachine.analytics.domain.model.MetaEstrategica;
import com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository.MetaEstrategicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class MetaEstrategicaService {

    private final MetaEstrategicaRepository repository;

    public Page<MetaEstrategica> listarTodos(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public MetaEstrategica buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Meta Estratégica não encontrada com o ID: " + id));
    }

    public Page<MetaEstrategica> buscarPorRoasMinimo(BigDecimal roas, Pageable pageable) {
        return repository.findByRoasAlvoGreaterThanEqual(roas, pageable);
    }

    public MetaEstrategica salvar(MetaEstrategica meta) {
        return repository.save(meta);
    }

    public MetaEstrategica atualizar(Long id, MetaEstrategica metaAtualizada) {
        MetaEstrategica existente = buscarPorId(id);
        existente.setOrcamentoMensal(metaAtualizada.getOrcamentoMensal());
        existente.setRoasAlvo(metaAtualizada.getRoasAlvo());
        existente.setContaAnunciante(metaAtualizada.getContaAnunciante());
        return repository.save(existente);
    }

    public void deletar(Long id) {
        buscarPorId(id);
        repository.deleteById(id);
    }
}