package com.growthmachine.analytics.config;

import com.growthmachine.analytics.service.IdempotencyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class IdempotencyInterceptor implements HandlerInterceptor {

    @Autowired
    private IdempotencyService idempotencyService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String key = request.getHeader("X-Idempotency-Key");
            if (key == null || idempotencyService.isDuplicate(key)) {
                response.setStatus(409); // Conflict
                return false;
            }
        }
        return true;
    }
}