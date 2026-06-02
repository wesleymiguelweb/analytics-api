package com.growthmachine.analytics.repository;

import com.growthmachine.analytics.model.MetaEstrategica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public interface MetaEstrategicaRepository extends JpaRepository<MetaEstrategica, Long> {

    /**
     * Busca metas estratégicas cujo ROAS alvo seja maior ou igual ao valor especificado, com paginação.
     */
    Page<MetaEstrategica> findByRoasAlvoGreaterThanEqual(BigDecimal roas, Pageable pageable);
}