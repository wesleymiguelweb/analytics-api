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
@Table(name = "tb_plataforma")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Entidade que representa a plataforma de anúncio")
public class Plataforma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da plataforma é obrigatório")
    @Column(nullable = false, unique = true, length = 100)
    @Schema(description = "Nome da plataforma de anúncios", example = "Google Ads")
    private String nome;

    @ManyToMany(mappedBy = "plataformas")
    @JsonIgnore
    @Builder.Default
    private List<Campanha> campanhas = new ArrayList<>();
}
