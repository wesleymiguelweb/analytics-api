package com.growthmachine.analytics.service;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class IdempotencyService {
    private final ConcurrentHashMap<String, Boolean> processedKeys = new ConcurrentHashMap<>();

    public boolean isDuplicate(String key) {
        return processedKeys.putIfAbsent(key, true) != null;
    }
}