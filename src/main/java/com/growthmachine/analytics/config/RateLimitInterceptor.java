package com.growthmachine.analytics.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final ConcurrentHashMap<String, AtomicInteger> counts = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 100;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = request.getRemoteAddr();
        int count = counts.computeIfAbsent(ip, k -> new AtomicInteger(0)).incrementAndGet();

        if (count > MAX_REQUESTS) {
            response.setStatus(429);
            response.setHeader("Retry-After", "60");
            response.getWriter().write("Limite de requisicoes excedido.");
            return false;
        }
        return true;
    }
}