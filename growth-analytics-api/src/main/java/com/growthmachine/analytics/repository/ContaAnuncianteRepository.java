package com.growthmachine.analytics.repository;

import com.growthmachine.analytics.model.ContaAnunciante;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContaAnuncianteRepository extends JpaRepository<ContaAnunciante, Long> {

    // O banco de dados entende essa linha
    Page<ContaAnunciante> findByNomeEmpresaContainingIgnoreCase(String nome, Pageable pageable);
}