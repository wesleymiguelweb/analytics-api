package com.growthmachine.analytics.service;

import com.growthmachine.analytics.model.Campanha;
import com.growthmachine.analytics.repository.CampanhaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CampanhaService {

    private final CampanhaRepository repository;

    public Campanha salvar(Campanha campanha) {
        return repository.save(campanha);
    }

    public Page<Campanha> listarTodos(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Optional<Campanha> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }
}