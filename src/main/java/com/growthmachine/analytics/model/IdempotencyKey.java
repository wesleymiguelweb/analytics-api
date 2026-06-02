package com.growthmachine.analytics.model;

import jakarta.persistence.*;
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
    private String idempotencyKey;

    @Id
    private String requestUri;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

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
        private String idempotencyKey;
        private String requestUri;
    }
}