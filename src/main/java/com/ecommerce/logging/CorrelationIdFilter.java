package com.ecommerce.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Correlation ID Filter
 * Generates unique correlation ID for each request and manages MDC context
 * Extends OncePerRequestFilter to ensure execution exactly once per request
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String USER_ID_KEY = "userId";
    private static final String SESSION_ID_KEY = "sessionId";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Generate unique correlation ID
            String correlationId = UUID.randomUUID().toString();

            // Store correlation ID in MDC
            MDC.put(CORRELATION_ID_KEY, correlationId);

            // Extract authenticated user information if available
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {

                // Store user ID in MDC
                String userId = authentication.getName();
                if (userId != null) {
                    MDC.put(USER_ID_KEY, userId);
                }
            }

            // Extract session ID if session exists
            HttpSession session = request.getSession(false);
            if (session != null) {
                String sessionId = session.getId();
                if (sessionId != null) {
                    MDC.put(SESSION_ID_KEY, sessionId);
                }
            }

            // Add correlation ID to response header
            response.setHeader(CORRELATION_ID_HEADER, correlationId);

            // Continue with filter chain
            filterChain.doFilter(request, response);

        } finally {
            // CRITICAL: Always clean up MDC to prevent memory leaks and context bleeding
            MDC.clear();
        }
    }
}
