package com.growthmachine.analytics.repository;

import com.growthmachine.analytics.model.SugestaoOtimizacao;
import com.growthmachine.analytics.model.enums.TipoAcao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SugestaoOtimizacaoRepository extends JpaRepository<SugestaoOtimizacao, Long> {
    // Filtra sugestões pelo tipo de ação (ex: buscar apenas as de "AUMENTAR_ORCAMENTO")
    Page<SugestaoOtimizacao> findByTipoAcao(com.growthmachine.analytics.model.enums.TipoAcao tipoAcao, org.springframework.data.domain.Pageable pageable);
    List<SugestaoOtimizacao> findByTipoAcao(TipoAcao tipoAcao);
}