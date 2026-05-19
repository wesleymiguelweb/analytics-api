package com.growthmachine.analytics.repository;

import com.growthmachine.analytics.model.Campanha;
import com.growthmachine.analytics.model.enums.CanalOrigem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampanhaRepository extends JpaRepository<Campanha, Long> {

    // Consulta personalizada exigida: Filtrar campanhas pela plataforma de origem
    List<Campanha> findByCanalOrigem(CanalOrigem canalOrigem);
}