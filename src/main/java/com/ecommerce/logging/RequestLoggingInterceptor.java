package com.ecommerce.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Request Logging Interceptor
 * Logs HTTP request/response details with latency tracking
 */
@Component
@RequiredArgsConstructor
public class RequestLoggingInterceptor implements HandlerInterceptor {

    private static final String REQUEST_START_TIME = "requestStartTime";

    /**
     * Pre-handle: Store request start time
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Record request start time
        request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());
        return true;
    }

    /**
     * After completion: Calculate latency and log request
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) {
        // Calculate latency
        Long startTime = (Long) request.getAttribute(REQUEST_START_TIME);
        long latency = 0;

        if (startTime != null) {
            latency = System.currentTimeMillis() - startTime;
        }

        // Extract request details
        String endpoint = request.getRequestURI();
        String method = request.getMethod();
        int status = response.getStatus();

        // Determine if this is an anomaly (error status code)
        boolean anomalyFlag = status >= 400;

        // Log the request using StructuredLogger
        StructuredLogger.logRequest(endpoint, method, status, latency, anomalyFlag);
    }
}
