package com.growthmachine.analytics.repository;

import com.growthmachine.analytics.model.MetricaDiaria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MetricaDiariaRepository extends JpaRepository<MetricaDiaria, Long> {

    // Consulta personalizada exigida: Buscar métricas por intervalo de datas (Excelente para gráficos)
    List<MetricaDiaria> findByDataBetween(LocalDate dataInicio, LocalDate dataFim);
}