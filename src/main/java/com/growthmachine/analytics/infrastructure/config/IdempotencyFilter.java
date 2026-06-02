package com.growthmachine.analytics.infrastructure.config;

import com.growthmachine.analytics.domain.model.IdempotencyKey;
import com.growthmachine.analytics.infrastructure.adapter.out.persistence.repository.IdempotencyKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.Optional;

@Component
public class IdempotencyFilter extends OncePerRequestFilter {

    private final IdempotencyKeyRepository repository;

    public IdempotencyFilter(IdempotencyKeyRepository repository) {
        this.repository = repository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Aplica o filtro apenas para métodos POST
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String idempotencyKeyStr = request.getHeader("X-Idempotency-Key");
        if (idempotencyKeyStr == null || idempotencyKeyStr.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Cria a chave composta para a busca
        IdempotencyKey.IdempotencyKeyId keyId = new IdempotencyKey.IdempotencyKeyId();
        keyId.setIdempotencyKey(idempotencyKeyStr);
        keyId.setRequestUri(request.getRequestURI());

        Optional<IdempotencyKey> existingKeyOpt = repository.findById(keyId);

        // Se a chave já existe para esta URI, retorna a resposta salva
        if (existingKeyOpt.isPresent()) {
            IdempotencyKey existingKey = existingKeyOpt.get();
            response.setStatus(existingKey.getResponseStatus());
            response.setContentType("application/json");
            response.getWriter().write(existingKey.getResponseBody());
            return;
        }

        // Envolve a requisição e a resposta para permitir a leitura do corpo
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, 1024 * 1024);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        filterChain.doFilter(wrappedRequest, wrappedResponse);

        int status = wrappedResponse.getStatus();
        // Salva a resposta apenas se a operação foi bem-sucedida (status 2xx)
        if (status >= 200 && status < 300) {
            String requestBody = new String(wrappedRequest.getContentAsByteArray());
            String responseBody = new String(wrappedResponse.getContentAsByteArray());

            IdempotencyKey newKey = new IdempotencyKey();
            newKey.setIdempotencyKey(idempotencyKeyStr);
            newKey.setRequestUri(request.getRequestURI());
            newKey.setRequestBody(requestBody);
            newKey.setResponseStatus(status);
            newKey.setResponseBody(responseBody);
            repository.save(newKey);
        }

        // Copia a resposta do wrapper para a resposta original
        wrappedResponse.copyBodyToResponse();
    }
}
