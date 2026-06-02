package com.growthmachine.analytics.infrastructure.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int LIMIT = 20;
    private static final long WINDOW_SECONDS = 60;
    private final Map<String, ClientWindow> clients = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!request.getRequestURI().startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientId = resolveClientId(request);
        long now = Instant.now().getEpochSecond();
        ClientWindow window = clients.compute(clientId, (key, current) -> {
            if (current == null || now >= current.resetAt()) {
                return new ClientWindow(now + WINDOW_SECONDS, 1);
            }
            return new ClientWindow(current.resetAt(), current.used() + 1);
        });

        long retryAfter = Math.max(0, window.resetAt() - now);
        response.setHeader("X-RateLimit-Limit", String.valueOf(LIMIT));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, LIMIT - window.used())));
        response.setHeader("X-RateLimit-Reset", String.valueOf(window.resetAt()));

        if (window.used() > LIMIT) {
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Limite de requisições excedido. Tente novamente mais tarde.\",\"status\":429}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveClientId(HttpServletRequest request) {
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.isBlank()) {
            return apiKey;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private record ClientWindow(long resetAt, int used) {
    }
}
