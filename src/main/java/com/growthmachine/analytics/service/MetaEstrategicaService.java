package com.growthmachine.analytics.service;

import com.growthmachine.analytics.model.MetaEstrategica;
import com.growthmachine.analytics.repository.MetaEstrategicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MetaEstrategicaService {

    private final MetaEstrategicaRepository repository;

    public MetaEstrategica salvar(MetaEstrategica meta) {
        return repository.save(meta);
    }

    public Page<MetaEstrategica> listarTodos(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Optional<MetaEstrategica> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }
}