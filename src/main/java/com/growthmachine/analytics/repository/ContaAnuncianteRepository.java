package com.growthmachine.analytics.repository;

import com.growthmachine.analytics.model.ContaAnunciante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContaAnuncianteRepository extends JpaRepository<ContaAnunciante, Long> {
    Optional<ContaAnunciante> findByNomeEmpresaContainingIgnoreCase(String nomeEmpresa);
}