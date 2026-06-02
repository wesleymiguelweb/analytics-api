package com.growthmachine.analytics.application.service;

import com.growthmachine.analytics.domain.model.ApiKey;
import com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository.ApiKeyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository repository;

    @Value("${analytics.api.fixed-key}")
    private String fixedKey;

    public ApiKey createKey(String owner) {
        String key = UUID.randomUUID().toString();
        ApiKey apiKey = new ApiKey(key, owner);
        return repository.save(apiKey);
    }

    public boolean isValid(String key) {
        return fixedKey.equals(key) || repository.findById(key).isPresent();
    }

    public void deleteKey(String key) {
        if (fixedKey.equals(key)) {
            return;
        }
        repository.deleteById(key);
    }
}
