package com.ecommerce.exception;

import com.ecommerce.logging.StructuredLogger;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global Exception Handler
 * Centralized exception handling with structured error logging.
 * Uses @ControllerAdvice to preserve Thymeleaf MVC flow.
 *
 * Error view mapping:
 * 404 → error/404
 * 403 → error/403
 * 500 → error/500
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    // ----------------------------------------------------------------
    // Business exceptions
    // ----------------------------------------------------------------

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
        }
        return buildMvcError("error/500", message, endpoint, HttpStatus.BAD_REQUEST);
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
        }
        return buildMvcError("error/404", message, endpoint, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle ProductNotFoundException
     */
    @ExceptionHandler(ProductNotFoundException.class)
    public Object handleProductNotFoundException(ProductNotFoundException ex, HttpServletRequest request) {
        String endpoint = request.getRequestURI();
        String message = ex.getMessage();
        StructuredLogger.logSystemEvent(message, ex, endpoint, true);

        if (isRestRequest(request)) {
            return buildErrorResponse(message, endpoint, HttpStatus.NOT_FOUND);
        }
        return buildMvcError("error/404", message, endpoint, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle OrderNotFoundException
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public Object handleOrderNotFoundException(OrderNotFoundException ex, HttpServletRequest request) {
        String endpoint = request.getRequestURI();
        String message = ex.getMessage();
        StructuredLogger.logSystemEvent(message, ex, endpoint, true);

        if (isRestRequest(request)) {
            return buildErrorResponse(message, endpoint, HttpStatus.NOT_FOUND);
        }
        return buildMvcError("error/404", message, endpoint, HttpStatus.NOT_FOUND);
    }

    /**
     * Handle EmptyCartCheckoutException
     */
    @ExceptionHandler(EmptyCartCheckoutException.class)
    public Object handleEmptyCartCheckoutException(EmptyCartCheckoutException ex, HttpServletRequest request) {
        String endpoint = request.getRequestURI();
        String message = ex.getMessage();
        StructuredLogger.logSystemEvent(message, ex, endpoint, true);

        if (isRestRequest(request)) {
            return buildErrorResponse(message, endpoint, HttpStatus.BAD_REQUEST);
        }
        return buildMvcError("error/500", message, endpoint, HttpStatus.BAD_REQUEST);
    }

    // ----------------------------------------------------------------
    // Routing / access exceptions
    // ----------------------------------------------------------------

    /**
     * Handle NoHandlerFoundException → 404
     * Triggered when spring.mvc.throw-exception-if-no-handler-found=true
     * and an unknown URL is requested.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public Object handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest request) {
        String endpoint = request.getRequestURI();
        // Not an anomaly — just a 404, no need to log as error
        if (isRestRequest(request)) {
            return buildErrorResponse("No handler found for " + endpoint, endpoint, HttpStatus.NOT_FOUND);
        }
        ModelAndView mav = new ModelAndView("error/404");
        mav.setStatus(HttpStatus.NOT_FOUND);
        mav.addObject("path", endpoint);
        mav.addObject("timestamp", LocalDateTime.now());
        return mav;
    }

    /**
     * Handle AccessDeniedException → 403
     * Thrown when a user tries to access a resource they don't own.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public Object handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        String endpoint = request.getRequestURI();
        String message = ex.getMessage() != null ? ex.getMessage() : "Access denied";
        StructuredLogger.logSystemEvent(message, ex, endpoint, true);

        if (isRestRequest(request)) {
            return buildErrorResponse(message, endpoint, HttpStatus.FORBIDDEN);
        }
        ModelAndView mav = new ModelAndView("error/403");
        mav.setStatus(HttpStatus.FORBIDDEN);
        mav.addObject("path", endpoint);
        mav.addObject("timestamp", LocalDateTime.now());
        return mav;
    }

    // ----------------------------------------------------------------
    // Generic fallback exceptions
    // ----------------------------------------------------------------

    /**
     * Handle RuntimeException
     */
    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        String endpoint = request.getRequestURI();
        String message = ex.getMessage() != null ? ex.getMessage() : "Runtime error occurred";
        StructuredLogger.logSystemEvent(message, ex, endpoint, true);

        if (isRestRequest(request)) {
            return buildErrorResponse(message, endpoint, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return buildMvcError("error/500", message, endpoint, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle all generic exceptions — catch-all fallback
     */
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception ex, HttpServletRequest request) {
        String endpoint = request.getRequestURI();
        String message = ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred";
        StructuredLogger.logSystemEvent(message, ex, endpoint, true);

        if (isRestRequest(request)) {
            return buildErrorResponse(message, endpoint, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return buildMvcError("error/500", message, endpoint, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ----------------------------------------------------------------
    // Helpers
    // ----------------------------------------------------------------

    /**
     * Build a ModelAndView for MVC error pages with HTTP status set.
     */
    private ModelAndView buildMvcError(String viewName, String message, String path, HttpStatus status) {
        ModelAndView mav = new ModelAndView(viewName);
        mav.setStatus(status);
        mav.addObject("error", message);
        mav.addObject("path", path);
        mav.addObject("status", status.value());
        mav.addObject("timestamp", LocalDateTime.now());
        return mav;
    }

    /**
     * Check if request expects JSON (REST client)
     */
    private boolean isRestRequest(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        return acceptHeader != null && acceptHeader.contains("application/json");
    }

    /**
     * Build JSON error response for REST endpoints
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(String message, String path, HttpStatus status) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", status.value());
        errorResponse.put("error", status.getReasonPhrase());
        errorResponse.put("message", message);
        errorResponse.put("path", path);
        return ResponseEntity.status(status).body(errorResponse);
    }
}
