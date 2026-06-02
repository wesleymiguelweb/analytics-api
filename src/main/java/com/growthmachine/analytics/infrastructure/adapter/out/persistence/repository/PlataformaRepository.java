package com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository;

import com.growthmachine.analytics.domain.model.Plataforma;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlataformaRepository extends JpaRepository<Plataforma, Long> {
    Page<Plataforma> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}