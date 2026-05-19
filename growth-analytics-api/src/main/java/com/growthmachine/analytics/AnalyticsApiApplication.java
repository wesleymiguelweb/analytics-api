package com.growthmachine.analytics;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        info = @Info(
                title = "Marketing de Crescimento",
                version = "2.0.0",
                description = "API desenvolvida para gestão de campanhas de tráfego pago, contas de anunciantes e análise de métricas diárias.",
                contact = @Contact(
                        name = "Wesley Miguel",
                        url = "https://github.com/wesleymiguelweb"
                )
        )
)
@SpringBootApplication
public class AnalyticsApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnalyticsApiApplication.class, args);
    }

}