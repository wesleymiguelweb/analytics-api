package com.growthmachine.analytics.service;

import com.growthmachine.analytics.model.ContaAnunciante;
import com.growthmachine.analytics.repository.ContaAnuncianteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ContaAnuncianteService {

    private final ContaAnuncianteRepository repository;

    public ContaAnunciante salvar(ContaAnunciante conta) {
        return repository.save(conta);
    }

    public Page<ContaAnunciante> listarTodos(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Optional<ContaAnunciante> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public void deletar(Long id) {
        repository.deleteById(id);
    }
}