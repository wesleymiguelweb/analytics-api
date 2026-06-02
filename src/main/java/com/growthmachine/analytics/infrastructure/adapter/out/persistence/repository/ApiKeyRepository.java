package com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository;

import com.growthmachine.analytics.domain.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, String> {
}