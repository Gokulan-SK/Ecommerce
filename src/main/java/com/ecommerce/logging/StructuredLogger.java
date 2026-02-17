package com.ecommerce.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import net.logstash.logback.argument.StructuredArguments;

import static net.logstash.logback.argument.StructuredArguments.entries;

import java.util.HashMap;
import java.util.Map;

public class StructuredLogger {

    private static final Logger requestLogger = LoggerFactory.getLogger("REQUEST_LOGGER");

    private static final Logger businessLogger = LoggerFactory.getLogger("BUSINESS_LOGGER");

    private static final Logger systemLogger = LoggerFactory.getLogger("SYSTEM_LOGGER");

    private StructuredLogger() {
    }

    /*
     * ============================================================
     * REQUEST EVENTS
     * ============================================================
     */
    public static void logRequest(String endpoint,
            String method,
            int status,
            long latency,
            boolean anomalyFlag) {

        Map<String, Object> logData = baseEvent("REQUEST", anomalyFlag);

        logData.put("endpoint", endpoint);
        logData.put("httpMethod", method);
        logData.put("httpStatus", status);
        logData.put("latencyMs", latency);

        requestLogger.info("{}", entries(logData));
    }

    /*
     * ============================================================
     * AOP BUSINESS EVENTS (Service Layer)
     * ============================================================
     */
    public static void logBusinessEvent(String className,
            String methodName,
            long executionTime) {

        Map<String, Object> logData = baseEvent("BUSINESS_EVENT", false);

        logData.put("className", className);
        logData.put("methodName", methodName);
        logData.put("executionTimeMs", executionTime);

        businessLogger.info("{}", entries(logData));
    }

    /*
     * ============================================================
     * SEMANTIC BUSINESS EVENTS (ML READY)
     * ============================================================
     */
    public static void logBusinessEvent(EventName eventName,
            Map<String, Object> businessFields) {

        Map<String, Object> logData = new HashMap<>();

        logData.put("eventType", "BUSINESS_EVENT");
        logData.put("eventName", eventName.name());
        logData.put("anomalyFlag", false);

        if (businessFields != null && !businessFields.isEmpty()) {
            logData.putAll(businessFields);
        }

        businessLogger.info("{}", StructuredArguments.entries(logData));
    }

    /*
     * ============================================================
     * SYSTEM EVENTS
     * ============================================================
     */
    public static void logSystemEvent(String message,
            Throwable throwable,
            String endpoint,
            boolean anomalyFlag) {

        Map<String, Object> logData = baseEvent("SYSTEM_EVENT", anomalyFlag);

        logData.put("message", message);

        if (endpoint != null) {
            logData.put("endpoint", endpoint);
        }

        if (throwable != null) {
            logData.put("exceptionClass", throwable.getClass().getName());
            logData.put("exceptionMessage", throwable.getMessage());
        }

        if (throwable != null) {
            systemLogger.error("{}", entries(logData), throwable);
        } else {
            systemLogger.error("{}", entries(logData));
        }
    }

    /*
     * ============================================================
     * BASE EVENT BUILDER (NOW INCLUDES MDC CONTEXT)
     * ============================================================
     */
    private static Map<String, Object> baseEvent(String eventType, boolean anomalyFlag) {
        Map<String, Object> logData = new HashMap<>();
        logData.put("eventType", eventType);
        logData.put("anomalyFlag", anomalyFlag);
        return logData;
    }

    /*
     * ============================================================
     * MDC CONTEXT INJECTION
     * ============================================================
     */
    private static void addMdcContext(Map<String, Object> logData) {

        String correlationId = MDC.get("correlationId");
        String userId = MDC.get("userId");
        String sessionId = MDC.get("sessionId");

        if (correlationId != null) {
            logData.put("correlationId", correlationId);
        }

        if (userId != null) {
            logData.put("userId", userId);
        }

        if (sessionId != null) {
            logData.put("sessionId", sessionId);
        }
    }
}