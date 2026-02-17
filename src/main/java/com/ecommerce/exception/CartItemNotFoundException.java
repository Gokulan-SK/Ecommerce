package com.ecommerce.exception;

/**
 * Exception thrown when a cart item is not found
 */
public class CartItemNotFoundException extends RuntimeException {

    public CartItemNotFoundException(String message) {
        super(message);
    }

    public CartItemNotFoundException(Long productId, String username) {
        super(String.format("Cart item not found for product ID %d in cart for user '%s'",
                productId, username));
    }
}
