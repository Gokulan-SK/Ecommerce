package com.ecommerce.exception;

import com.ecommerce.logging.StructuredLogger;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 * Centralized exception handling with structured error logging
 * Uses @ControllerAdvice to preserve Thymeleaf MVC flow
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle InsufficientStockException
     */
    @ExceptionHandler(InsufficientStockException.class)
    public Object handleInsufficientStockException(InsufficientStockException ex, HttpServletRequest request) {
        String endpoint = request.getRequestURI();
        String message = ex.getMessage();

        StructuredLogger.logSystemEvent(message, ex, endpoint, true);

        if (isRestRequest(request)) {
            return buildErrorResponse(message, endpoint, HttpStatus.BAD_REQUEST);
        } else {
            ModelAndView modelAndView = new ModelAndView("error");
            modelAndView.addObject("error", message);
            modelAndView.addObject("path", endpoint);
            modelAndView.addObject("timestamp", LocalDateTime.now());
            modelAndView.addObject("status", HttpStatus.BAD_REQUEST.value());
            return modelAndView;
        }
    }

    /**
     * Handle CartItemNotFoundException
     */
    @ExceptionHandler(CartItemNotFoundException.class)
    public Object handleCartItemNotFoundException(CartItemNotFoundException ex, HttpServletRequest request) {
        String endpoint = request.getRequestURI();
        String message = ex.getMessage();

        StructuredLogger.logSystemEvent(message, ex, endpoint, true);

        if (isRestRequest(request)) {
            return buildErrorResponse(message, endpoint, HttpStatus.NOT_FOUND);
        } else {
            ModelAndView modelAndView = new ModelAndView("error");
            modelAndView.addObject("error", message);
            modelAndView.addObject("path", endpoint);
            modelAndView.addObject("timestamp", LocalDateTime.now());
            modelAndView.addObject("status", HttpStatus.NOT_FOUND.value());
            return modelAndView;
        }
    }

    /**
     * Handle ProductNotFoundException
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public Object handleProductNotFoundException(ProductNotFoundException ex, HttpServletRequest request) {
        String endpoint = request.getRequestURI();
        String message = ex.getMessage();

        // Log the exception
        StructuredLogger.logSystemEvent(message, ex, endpoint, true);

        // Check if this is a REST/JSON request or MVC request
        if (isRestRequest(request)) {
            return buildErrorResponse(message, endpoint, HttpStatus.NOT_FOUND);
        } else {
            // Return error view for MVC endpoints
            ModelAndView modelAndView = new ModelAndView("error");
            modelAndView.addObject("error", message);
            modelAndView.addObject("path", endpoint);
            modelAndView.addObject("timestamp", LocalDateTime.now());
            modelAndView.addObject("status", HttpStatus.NOT_FOUND.value());
            return modelAndView;
        }
    }

    /**
     * Handle all generic exceptions
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception ex, HttpServletRequest request) {
        // Extract request details
        String endpoint = request.getRequestURI();
        String message = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred";

        // Log the exception with anomaly flag
        StructuredLogger.logSystemEvent(message, ex, endpoint, true);

        // Check if this is a REST/JSON request or MVC request
        if (isRestRequest(request)) {
            // Return JSON response for REST endpoints
            return buildErrorResponse(message, endpoint, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            // Return error view for MVC endpoints
            ModelAndView modelAndView = new ModelAndView("error");
            modelAndView.addObject("error", message);
            modelAndView.addObject("path", endpoint);
            modelAndView.addObject("timestamp", LocalDateTime.now());
            return modelAndView;
        }
    }

    /**
     * Handle RuntimeException specifically
     */
    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        // Extract request details
        String endpoint = request.getRequestURI();
        String message = ex.getMessage() != null ? ex.getMessage() : "Runtime error occurred";

        // Log the exception with anomaly flag
        StructuredLogger.logSystemEvent(message, ex, endpoint, true);

        // Check if this is a REST/JSON request or MVC request
        if (isRestRequest(request)) {
            // Return JSON response for REST endpoints
            return buildErrorResponse(message, endpoint, HttpStatus.INTERNAL_SERVER_ERROR);
        } else {
            // Return error view for MVC endpoints
            ModelAndView modelAndView = new ModelAndView("error");
            modelAndView.addObject("error", message);
            modelAndView.addObject("path", endpoint);
            modelAndView.addObject("timestamp", LocalDateTime.now());
            return modelAndView;
        }
    }

    /**
     * Determine if the request expects JSON response
     */
    private boolean isRestRequest(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        String contentType = request.getContentType();

        // Check if request accepts or sends JSON
        return (acceptHeader != null && acceptHeader.contains("application/json"))
                || (contentType != null && contentType.contains("application/json"));
    }

    /**
     * Build error response for REST endpoints
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, String path, HttpStatus status) {
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        errorDetails.put("status", status.value());
        errorDetails.put("error", status.getReasonPhrase());
        errorDetails.put("message", message);
        errorDetails.put("path", path);

        return ResponseEntity.status(status).body(errorDetails);
    }
}
