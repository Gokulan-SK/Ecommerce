package com.ecommerce.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Service Logging Aspect
 * Automatically logs method execution in service layer using AOP
 */
@Aspect
@Component
public class ServiceLoggingAspect {

    /**
     * Around advice for all methods in service package
     * Logs method entry, exit, and execution time
     */
    @Around("execution(* com.ecommerce.service..*.*(..))")
    public Object logServiceExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        // Extract method details
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        // Record start time
        long startTime = System.currentTimeMillis();

        try {
            // Proceed with method execution
            Object result = joinPoint.proceed();

            // Calculate execution time
            long executionTime = System.currentTimeMillis() - startTime;

            // Log business event
            StructuredLogger.logBusinessEvent(className, methodName, executionTime);

            return result;

        } catch (Throwable throwable) {
            // Calculate execution time even on failure
            long executionTime = System.currentTimeMillis() - startTime;

            // Log business event (will be logged, but exception will be re-thrown)
            StructuredLogger.logBusinessEvent(className, methodName, executionTime);

            // Re-throw the exception so it can be handled by GlobalExceptionHandler
            throw throwable;
        }
    }
}
