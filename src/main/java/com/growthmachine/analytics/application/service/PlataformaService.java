package com.growthmachine.analytics.application.service;

import com.growthmachine.analytics.domain.exception.ResourceNotFoundException;
import com.growthmachine.analytics.domain.model.Plataforma;
import com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository.PlataformaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlataformaService {

    private final PlataformaRepository repository;

    public Page<Plataforma> listar(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Plataforma buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Plataforma não encontrada com o ID: " + id));
    }

    public Plataforma criar(Plataforma plataforma) {
        // Validações de negócio podem ser adicionadas aqui
        return repository.save(plataforma);
    }

    public Plataforma atualizar(Long id, Plataforma plataformaAtualizada) {
        Plataforma existente = buscar(id); // Reutiliza o método que já lança exceção
        existente.setNome(plataformaAtualizada.getNome());
        return repository.save(existente);
    }

    public void deletar(Long id) {
        buscar(id); // Garante que a plataforma existe antes de tentar deletar
        repository.deleteById(id);
    }

    public Page<Plataforma> buscarPorNome(String nome, Pageable pageable) {
        return repository.findByNomeContainingIgnoreCase(nome, pageable);
    }
}