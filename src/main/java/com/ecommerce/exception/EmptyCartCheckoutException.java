package com.ecommerce.exception;

/**
 * Exception thrown when attempting to checkout with an empty cart
 */
public class EmptyCartCheckoutException extends RuntimeException {

    public EmptyCartCheckoutException(String message) {
        super(message);
    }
}
