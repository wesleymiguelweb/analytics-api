package com.growthmachine.analytics.repository;

import com.growthmachine.analytics.model.Campanha;
import com.growthmachine.analytics.model.enums.CanalOrigem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampanhaRepository extends JpaRepository<Campanha, Long> {
    /**
     * Busca campanhas por canal de origem com paginação.
     */
    Page<Campanha> findByCanalOrigem(CanalOrigem canal, Pageable pageable);
}