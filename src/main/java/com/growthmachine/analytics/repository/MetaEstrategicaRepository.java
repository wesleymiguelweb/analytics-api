package com.growthmachine.analytics.repository;

import com.growthmachine.analytics.model.MetaEstrategica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface MetaEstrategicaRepository extends JpaRepository<MetaEstrategica, Long> {

    // Consulta personalizada: Buscar metas que possuam orçamento maior ou igual ao valor informado
    List<MetaEstrategica> findByOrcamentoMensalGreaterThanEqual(BigDecimal valorMinimo);
}