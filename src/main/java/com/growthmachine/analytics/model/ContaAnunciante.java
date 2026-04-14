package com.growthmachine.analytics.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.hateoas.RepresentationModel;
import java.util.List;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "tb_conta_anunciante")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidade que representa a empresa anunciante")
public class ContaAnunciante extends RepresentationModel<ContaAnunciante> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da empresa é obrigatório")
    @Column(nullable = false, unique = true, length = 100)
    private String nomeEmpresa;
    @OneToMany(mappedBy = "contaAnunciante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore // Evita loop infinito no Swagger
    private List<Campanha> campanhas = new ArrayList<>();
}