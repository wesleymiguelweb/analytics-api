package com.growthmachine.analytics.application.service;

import com.growthmachine.analytics.domain.exception.ResourceNotFoundException;
import com.growthmachine.analytics.domain.model.MetricaDiaria;
import com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository.MetricaDiariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MetricaDiariaService {

    private final MetricaDiariaRepository repository;

    public Page<MetricaDiaria> listarTodos(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public MetricaDiaria buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Métrica Diária não encontrada com o ID: " + id));
    }

    public Page<MetricaDiaria> buscarPorData(LocalDate data, Pageable pageable) {
        return repository.findByData(data, pageable);
    }

    public Page<MetricaDiaria> buscarPorDataEntre(LocalDate dataInicio, LocalDate dataFim, Pageable pageable) {
        return repository.findByDataBetween(dataInicio, dataFim, pageable);
    }

    public MetricaDiaria salvar(MetricaDiaria metrica) {
        return repository.save(metrica);
    }

    public MetricaDiaria atualizar(Long id, MetricaDiaria metricaAtualizada) {
        MetricaDiaria existente = buscarPorId(id);
        existente.setData(metricaAtualizada.getData());
        existente.setCliques(metricaAtualizada.getCliques());
        existente.setImpressoes(metricaAtualizada.getImpressoes());
        existente.setCusto(metricaAtualizada.getCusto());
        existente.setConversoes(metricaAtualizada.getConversoes());
        existente.setGmv(metricaAtualizada.getGmv());
        existente.setCampanha(metricaAtualizada.getCampanha());
        return repository.save(existente);
    }

    public void deletar(Long id) {
        buscarPorId(id);
        repository.deleteById(id);
    }
}
