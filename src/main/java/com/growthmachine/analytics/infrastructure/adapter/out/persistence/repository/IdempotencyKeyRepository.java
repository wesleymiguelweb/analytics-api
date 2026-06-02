package com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository;

import com.growthmachine.analytics.domain.model.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, IdempotencyKey.IdempotencyKeyId> {
}
