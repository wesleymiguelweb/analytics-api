package com.growthmachine.analytics.repository;

import com.growthmachine.analytics.model.Campanha;
import com.growthmachine.analytics.model.enums.CanalOrigem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CampanhaRepository extends JpaRepository<Campanha, Long> {
    Page<Campanha> findByCanalOrigem(String canal, Pageable pageable);
    List<Campanha> findByCanalOrigem(CanalOrigem canalOrigem);
}