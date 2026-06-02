package com.growthmachine.analytics.domain.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_idempotency_key")
@Data
@NoArgsConstructor
@AllArgsConstructor
@IdClass(IdempotencyKey.IdempotencyKeyId.class) // Define a chave primária composta
public class IdempotencyKey {

    @Id
    @NotBlank(message = "A chave idempotente é obrigatória")
    private String idempotencyKey;

    @Id
    @NotBlank(message = "A URI da requisição é obrigatória")
    private String requestUri;

    @NotNull(message = "A data de criação é obrigatória")
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @NotNull(message = "O corpo da requisição não pode ser nulo")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String requestBody;

    @Column(nullable = false)
    private int responseStatus;

    @Column(columnDefinition = "TEXT")
    private String responseBody;

    // Classe interna para a chave primária composta
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdempotencyKeyId implements Serializable {
        @NotBlank(message = "A chave idempotente é obrigatória")
        private String idempotencyKey;

        @NotBlank(message = "A URI da requisição é obrigatória")
        private String requestUri;
    }
}
