package com.growthmachine.analytics.repository;

import com.growthmachine.analytics.model.MetricaDiaria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MetricaDiariaRepository extends JpaRepository<MetricaDiaria, Long> {

    Page<MetricaDiaria> findByData(java.time.LocalDate data, Pageable pageable);
    List<MetricaDiaria> findByDataBetween(LocalDate dataInicio, LocalDate dataFim);
}