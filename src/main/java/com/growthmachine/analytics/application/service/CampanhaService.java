package com.growthmachine.analytics.application.service;

import com.growthmachine.analytics.domain.exception.ResourceNotFoundException;
import com.growthmachine.analytics.domain.model.Campanha;
import com.growthmachine.analytics.domain.model.enums.CanalOrigem;
import com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository.CampanhaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CampanhaService {

    private final CampanhaRepository repository;

    public Page<Campanha> listarTodos(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Campanha buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Campanha não encontrada com o ID: " + id));
    }

    public Campanha salvar(Campanha campanha) {
        // Aqui você poderia adicionar validações de negócio, se necessário
        return repository.save(campanha);
    }

    public Campanha atualizar(Long id, Campanha campanhaAtualizada) {
        Campanha existente = buscarPorId(id);
        existente.setNomeCampanha(campanhaAtualizada.getNomeCampanha());
        existente.setCanalOrigem(campanhaAtualizada.getCanalOrigem());
        existente.setContaAnunciante(campanhaAtualizada.getContaAnunciante());
        return repository.save(existente);
    }

    public void deletar(Long id) {
        buscarPorId(id); // Garante que a campanha existe antes de deletar
        repository.deleteById(id);
    }

    public Page<Campanha> buscarPorCanal(CanalOrigem canal, Pageable pageable) {
        return repository.findByCanalOrigem(canal, pageable);
    }

    public Page<Campanha> buscarPorNome(String nome, Pageable pageable) {
        return repository.findByNomeCampanhaContainingIgnoreCase(nome, pageable);
    }
}