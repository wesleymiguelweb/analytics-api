package com.growthmachine.analytics.repository;

import com.growthmachine.analytics.model.SugestaoOtimizacao;
import com.growthmachine.analytics.model.enums.TipoAcao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SugestaoOtimizacaoRepository extends JpaRepository<SugestaoOtimizacao, Long> {
    // Consulta personalizada exigida
    List<SugestaoOtimizacao> findByTipoAcao(TipoAcao tipoAcao);
}