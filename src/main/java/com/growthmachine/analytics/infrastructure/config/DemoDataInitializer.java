package com.growthmachine.analytics.infrastructure.config;

import com.growthmachine.analytics.domain.model.Campanha;
import com.growthmachine.analytics.domain.model.ContaAnunciante;
import com.growthmachine.analytics.domain.model.MetaEstrategica;
import com.growthmachine.analytics.domain.model.MetricaDiaria;
import com.growthmachine.analytics.domain.model.Plataforma;
import com.growthmachine.analytics.domain.model.enums.CanalOrigem;
import com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository.CampanhaRepository;
import com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository.ContaAnuncianteRepository;
import com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository.MetaEstrategicaRepository;
import com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository.MetricaDiariaRepository;
import com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository.PlataformaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {

    private final ContaAnuncianteRepository contaRepository;
    private final PlataformaRepository plataformaRepository;
    private final CampanhaRepository campanhaRepository;
    private final MetaEstrategicaRepository metaRepository;
    private final MetricaDiariaRepository metricaRepository;

    @Override
    public void run(String... args) {
        if (campanhaRepository.count() > 0 || metricaRepository.count() > 0) {
            return;
        }

        ContaAnunciante conta = contaRepository.save(ContaAnunciante.builder()
                .nomeEmpresa("Growth Machine Demo")
                .build());

        metaRepository.save(MetaEstrategica.builder()
                .orcamentoMensal(new BigDecimal("12000.00"))
                .roasAlvo(new BigDecimal("3.50"))
                .contaAnunciante(conta)
                .build());

        Plataforma googleAds = plataformaRepository.save(Plataforma.builder()
                .nome("Google Ads")
                .build());
        Plataforma metaAds = plataformaRepository.save(Plataforma.builder()
                .nome("Meta Ads")
                .build());

        Campanha campanha = campanhaRepository.save(Campanha.builder()
                .nomeCampanha("Demo - Performance Ecommerce")
                .canalOrigem(CanalOrigem.GOOGLE_ADS)
                .contaAnunciante(conta)
                .plataformas(List.of(googleAds, metaAds))
                .build());

        metricaRepository.saveAll(List.of(
                metrica(LocalDate.of(2026, 3, 1), 420, 18000, "520.00", 18, "2350.00", campanha),
                metrica(LocalDate.of(2026, 3, 2), 510, 21000, "640.00", 24, "3180.00", campanha),
                metrica(LocalDate.of(2026, 3, 3), 390, 17000, "480.00", 15, "1890.00", campanha),
                metrica(LocalDate.of(2026, 3, 4), 610, 24000, "760.00", 31, "4200.00", campanha),
                metrica(LocalDate.of(2026, 3, 5), 455, 19500, "570.00", 21, "2780.00", campanha)
        ));
    }

    private MetricaDiaria metrica(LocalDate data, int cliques, int impressoes, String custo, int conversoes,
                                  String gmv, Campanha campanha) {
        return MetricaDiaria.builder()
                .data(data)
                .cliques(cliques)
                .impressoes(impressoes)
                .custo(new BigDecimal(custo))
                .conversoes(conversoes)
                .gmv(new BigDecimal(gmv))
                .campanha(campanha)
                .build();
    }
}
