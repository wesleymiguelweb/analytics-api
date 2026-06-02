package com.growthmachine.analytics.domain.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_conta_anunciante")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidade que representa a empresa anunciante")
public class ContaAnunciante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da empresa é obrigatório")
    @Column(nullable = false, unique = true, length = 100)
    @Schema(description = "Nome da empresa anunciante", example = "Growth Machine")
    private String nomeEmpresa;

    @OneToMany(mappedBy = "contaAnunciante", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    @Builder.Default
    private List<Campanha> campanhas = new ArrayList<>();
}
