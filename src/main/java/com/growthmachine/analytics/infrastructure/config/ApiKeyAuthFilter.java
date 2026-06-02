package com.growthmachine.analytics.infrastructure.config;

import com.growthmachine.analytics.application.service.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Ignora a validação para a documentação do Swagger e recursos estáticos
        String path = request.getRequestURI();
        if (isPublicRequest(request, path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKeyHeader = request.getHeader("X-API-Key");

        if (apiKeyHeader == null || apiKeyHeader.isEmpty() || !apiKeyService.isValid(apiKeyHeader)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Acesso negado: Chave de API (X-API-Key) inválida ou ausente.\",\"status\":401}");
            return;
        }

        PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(
                apiKeyHeader,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }

    private boolean isPublicRequest(HttpServletRequest request, String path) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/h2-console")
                || path.equals("/")
                || path.equals("/index.html")
                || path.startsWith("/webjars")
                || ("POST".equalsIgnoreCase(request.getMethod()) && path.equals("/api/keys"));
    }
}
