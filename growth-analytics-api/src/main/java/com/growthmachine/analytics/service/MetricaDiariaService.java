package com.growthmachine.analytics.service;

import com.growthmachine.analytics.model.MetricaDiaria;
import com.growthmachine.analytics.repository.MetricaDiariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MetricaDiariaService {

    private final MetricaDiariaRepository repository;

    public MetricaDiaria salvar(MetricaDiaria metrica) {
        return repository.save(metrica);
    }

    public Page<MetricaDiaria> listarTodos(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Optional<MetricaDiaria> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }
}