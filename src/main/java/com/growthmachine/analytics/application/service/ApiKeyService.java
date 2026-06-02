package com.growthmachine.analytics.application.service;

import com.growthmachine.analytics.domain.model.ApiKey;
import com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository.ApiKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ApiKeyService {

    @Autowired
    private ApiKeyRepository repository;

    public ApiKey createKey(String owner) {
        String key = UUID.randomUUID().toString();
        ApiKey apiKey = new ApiKey(key, owner);
        return repository.save(apiKey);
    }

    public boolean isValid(String key) {
        return repository.findById(key).isPresent();
    }

    public void deleteKey(String key) {
        repository.deleteById(key);
    }
}