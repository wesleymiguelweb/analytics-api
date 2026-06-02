package com.growthmachine.analytics.application.service;

import com.growthmachine.analytics.application.dto.IndicadoresCampanhaResponse;
import com.growthmachine.analytics.application.dto.SugestaoAnaliticaResponse;
import com.growthmachine.analytics.domain.exception.ResourceNotFoundException;
import com.growthmachine.analytics.domain.model.Campanha;
import com.growthmachine.analytics.domain.model.MetaEstrategica;
import com.growthmachine.analytics.domain.model.MetricaDiaria;
import com.growthmachine.analytics.domain.model.SugestaoOtimizacao;
import com.growthmachine.analytics.domain.model.enums.TipoAcao;
import com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository.CampanhaRepository;
import com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository.MetaEstrategicaRepository;
import com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository.MetricaDiariaRepository;
import com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository.SugestaoOtimizacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GrowthAnalyticsService {

    private static final BigDecimal DEFAULT_ROAS_ALVO = new BigDecimal("3.00");
    private static final BigDecimal CTR_MINIMO = new BigDecimal("1.00");
    private static final BigDecimal CTR_SAUDAVEL = new BigDecimal("1.50");
    private static final BigDecimal TAXA_CONVERSAO_MINIMA = new BigDecimal("2.00");
    private static final int CONVERSOES_MINIMAS_PARA_ESCALA = 5;

    private final CampanhaRepository campanhaRepository;
    private final MetricaDiariaRepository metricaRepository;
    private final MetaEstrategicaRepository metaRepository;
    private final SugestaoOtimizacaoRepository sugestaoRepository;

    public IndicadoresCampanhaResponse calcularIndicadores(Long campanhaId, LocalDate dataInicio, LocalDate dataFim) {
        validarPeriodo(dataInicio, dataFim);
        Campanha campanha = buscarCampanha(campanhaId);
        List<MetricaDiaria> metricas = metricaRepository.findByCampanhaIdAndDataBetween(campanhaId, dataInicio, dataFim);

        int impressoes = metricas.stream().mapToInt(MetricaDiaria::getImpressoes).sum();
        int cliques = metricas.stream().mapToInt(MetricaDiaria::getCliques).sum();
        int conversoes = metricas.stream().mapToInt(MetricaDiaria::getConversoes).sum();
        BigDecimal custo = somar(metricas.stream().map(MetricaDiaria::getCusto).toList());
        BigDecimal gmv = somar(metricas.stream().map(MetricaDiaria::getGmv).toList());
        BigDecimal roasAlvo = buscarRoasAlvo(campanha);

        BigDecimal ctr = percentual(cliques, impressoes);
        BigDecimal taxaConversao = percentual(conversoes, cliques);
        BigDecimal cpc = dividir(custo, cliques);
        BigDecimal cpa = dividir(custo, conversoes);
        BigDecimal ticketMedio = dividir(gmv, conversoes);
        BigDecimal roas = dividir(gmv, custo);
        BigDecimal margemRoas = roas.subtract(roasAlvo).setScale(2, RoundingMode.HALF_UP);
        List<String> alertas = gerarAlertas(metricas, ctr, taxaConversao, roas, roasAlvo, conversoes, custo);

        return new IndicadoresCampanhaResponse(
                campanha.getId(),
                campanha.getNomeCampanha(),
                dataInicio,
                dataFim,
                metricas.size(),
                impressoes,
                cliques,
                conversoes,
                custo,
                gmv,
                ctr,
                taxaConversao,
                cpc,
                cpa,
                ticketMedio,
                roas,
                roasAlvo,
                margemRoas,
                diagnosticar(metricas, roas, roasAlvo, ctr, taxaConversao, conversoes, custo),
                alertas
        );
    }

    public SugestaoAnaliticaResponse gerarSugestao(Long campanhaId, LocalDate dataInicio, LocalDate dataFim) {
        IndicadoresCampanhaResponse indicadores = calcularIndicadores(campanhaId, dataInicio, dataFim);
        TipoAcao tipoAcao = escolherTipoAcao(indicadores);
        List<String> justificativas = montarJustificativas(indicadores);

        return new SugestaoAnaliticaResponse(
                tipoAcao,
                montarDescricao(tipoAcao, indicadores),
                indicadores,
                justificativas
        );
    }

    public SugestaoOtimizacao gerarESalvarSugestao(Long campanhaId, LocalDate dataInicio, LocalDate dataFim) {
        Campanha campanha = buscarCampanha(campanhaId);
        SugestaoAnaliticaResponse sugestaoAnalitica = gerarSugestao(campanhaId, dataInicio, dataFim);

        SugestaoOtimizacao sugestao = new SugestaoOtimizacao();
        sugestao.setTipoAcao(sugestaoAnalitica.tipoAcao());
        sugestao.setDescricao(sugestaoAnalitica.descricao());
        sugestao.setCampanhas(List.of(campanha));
        return sugestaoRepository.save(sugestao);
    }

    private Campanha buscarCampanha(Long campanhaId) {
        return campanhaRepository.findById(campanhaId)
                .orElseThrow(() -> new ResourceNotFoundException("Campanha não encontrada com o ID: " + campanhaId));
    }

    private BigDecimal buscarRoasAlvo(Campanha campanha) {
        if (campanha.getContaAnunciante() == null || campanha.getContaAnunciante().getId() == null) {
            return DEFAULT_ROAS_ALVO;
        }

        return metaRepository.findByContaAnuncianteId(campanha.getContaAnunciante().getId())
                .map(MetaEstrategica::getRoasAlvo)
                .orElse(DEFAULT_ROAS_ALVO);
    }

    private void validarPeriodo(LocalDate dataInicio, LocalDate dataFim) {
        if (dataInicio == null || dataFim == null) {
            throw new IllegalArgumentException("Informe dataInicio e dataFim no formato YYYY-MM-DD.");
        }
        if (dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("dataInicio não pode ser maior que dataFim.");
        }
    }

    private BigDecimal somar(List<BigDecimal> valores) {
        return valores.stream()
                .filter(valor -> valor != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal percentual(int numerador, int denominador) {
        if (denominador == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(numerador)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominador), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal dividir(BigDecimal numerador, int denominador) {
        if (numerador == null || denominador == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return numerador.divide(BigDecimal.valueOf(denominador), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal dividir(BigDecimal numerador, BigDecimal denominador) {
        if (numerador == null || denominador == null || denominador.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return numerador.divide(denominador, 2, RoundingMode.HALF_UP);
    }

    private List<String> gerarAlertas(List<MetricaDiaria> metricas, BigDecimal ctr, BigDecimal taxaConversao,
                                      BigDecimal roas, BigDecimal roasAlvo, int conversoes, BigDecimal custo) {
        List<String> alertas = new ArrayList<>();
        if (metricas.isEmpty()) {
            alertas.add("Nenhum dado mensurado foi encontrado para o período.");
            return alertas;
        }
        if (roas.compareTo(roasAlvo) < 0) {
            alertas.add("ROAS abaixo da meta estratégica.");
        }
        if (ctr.compareTo(CTR_MINIMO) < 0) {
            alertas.add("CTR baixo: criativo, promessa ou segmentação podem estar limitando o tráfego.");
        }
        if (taxaConversao.compareTo(TAXA_CONVERSAO_MINIMA) < 0) {
            alertas.add("Taxa de conversão baixa: revisar público, oferta, página ou qualidade do tráfego.");
        }
        if (conversoes == 0 && custo.compareTo(BigDecimal.ZERO) > 0) {
            alertas.add("Investimento com zero conversões no período.");
        }
        if (alertas.isEmpty()) {
            alertas.add("Campanha saudável para o período analisado.");
        }
        return alertas;
    }

    private String diagnosticar(List<MetricaDiaria> metricas, BigDecimal roas, BigDecimal roasAlvo, BigDecimal ctr,
                                BigDecimal taxaConversao, int conversoes, BigDecimal custo) {
        if (metricas.isEmpty()) {
            return "SEM_DADOS";
        }
        if (conversoes == 0 && custo.compareTo(BigDecimal.ZERO) > 0) {
            return "PAUSAR_AVALIAR";
        }
        if (roas.compareTo(roasAlvo.multiply(new BigDecimal("1.20"))) >= 0 && conversoes >= CONVERSOES_MINIMAS_PARA_ESCALA) {
            return "ESCALAR";
        }
        if (roas.compareTo(roasAlvo) < 0 || ctr.compareTo(CTR_MINIMO) < 0 || taxaConversao.compareTo(TAXA_CONVERSAO_MINIMA) < 0) {
            return "OTIMIZAR";
        }
        return "MANTER_MONITORAR";
    }

    private TipoAcao escolherTipoAcao(IndicadoresCampanhaResponse indicadores) {
        if ("ESCALAR".equals(indicadores.diagnostico())) {
            return TipoAcao.AUMENTAR_ORCAMENTO;
        }
        if ("PAUSAR_AVALIAR".equals(indicadores.diagnostico())) {
            return TipoAcao.PAUSAR_CAMPANHA;
        }
        if (indicadores.ctr().compareTo(CTR_MINIMO) < 0) {
            return TipoAcao.CRIAR_NOVO_ANUNCIO;
        }
        if (indicadores.roas().compareTo(indicadores.roasAlvo()) < 0) {
            return TipoAcao.DIMINUIR_ORCAMENTO;
        }
        return TipoAcao.AJUSTAR_LANCES;
    }

    private List<String> montarJustificativas(IndicadoresCampanhaResponse indicadores) {
        List<String> justificativas = new ArrayList<>(indicadores.alertas());
        justificativas.add("ROAS realizado: " + indicadores.roas() + " vs meta: " + indicadores.roasAlvo() + ".");
        justificativas.add("CTR: " + indicadores.ctr() + "%; taxa de conversão: " + indicadores.taxaConversao() + "%; CPA: " + indicadores.cpa() + ".");
        return justificativas;
    }

    private String montarDescricao(TipoAcao tipoAcao, IndicadoresCampanhaResponse indicadores) {
        return switch (tipoAcao) {
            case AUMENTAR_ORCAMENTO -> "Aumentar orçamento gradualmente porque a campanha está acima do ROAS alvo e possui volume mínimo de conversões.";
            case DIMINUIR_ORCAMENTO -> "Reduzir orçamento e redistribuir verba porque o ROAS está abaixo da meta estratégica.";
            case PAUSAR_CAMPANHA -> "Pausar a campanha para diagnóstico porque houve investimento sem conversões no período.";
            case CRIAR_NOVO_ANUNCIO -> "Criar novos anúncios ou revisar criativos porque o CTR indica baixa atratividade da mensagem.";
            case AJUSTAR_LANCES -> "Ajustar lances, público ou posicionamentos para melhorar eficiência sem interromper completamente a campanha.";
        };
    }
}
