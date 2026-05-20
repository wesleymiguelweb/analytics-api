package com.growthmachine.analytics.repository;

import com.growthmachine.analytics.model.MetaEstrategica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MetaEstrategicaRepository extends JpaRepository<MetaEstrategica, Long> {

    Page<MetaEstrategica> findByRoasAlvoGreaterThanEqual(Double roas, Pageable pageable);
    List<MetaEstrategica> findByOrcamentoMensalGreaterThanEqual(BigDecimal valorMinimo);
}