package com.growthmachine.analytics.application.service;

import com.growthmachine.analytics.domain.exception.ResourceNotFoundException;
import com.growthmachine.analytics.domain.model.ContaAnunciante;
import com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository.ContaAnuncianteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ContaAnuncianteService {

    private final ContaAnuncianteRepository repository;

    public Page<ContaAnunciante> listarTodos(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public ContaAnunciante buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta Anunciante não encontrada com o ID: " + id));
    }

    public Page<ContaAnunciante> buscarPorNome(String nome, Pageable pageable) {
        return repository.findByNomeEmpresaContainingIgnoreCase(nome, pageable);
    }

    public ContaAnunciante salvar(ContaAnunciante conta) {
        return repository.save(conta);
    }

    public ContaAnunciante atualizar(Long id, ContaAnunciante contaAtualizada) {
        ContaAnunciante existente = buscarPorId(id); // Reutiliza a busca e a exceção
        existente.setNomeEmpresa(contaAtualizada.getNomeEmpresa());
        return repository.save(existente);
    }

    public void deletar(Long id) {
        buscarPorId(id); // Reutiliza a busca para garantir que o recurso existe antes de deletar
        repository.deleteById(id);
    }
}