package com.ecommerce.config;

import com.ecommerce.service.CartService;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Global Model Attributes
 * Provides common attributes to all controllers
 */
@ControllerAdvice
public class GlobalModelAttributesAdvice {

    private final CartService cartService;

    public GlobalModelAttributesAdvice(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Add cart item count to all views
     * Shows total quantity of items in cart
     */
    @ModelAttribute("cartItemCount")
    public int cartItemCount() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication != null && authentication.isAuthenticated()
                    && !"anonymousUser".equals(authentication.getPrincipal())) {
                String username = authentication.getName();
                return cartService.getCartItemCount(username);
            }
        } catch (Exception e) {
            // Return 0 if error
        }
        return 0;
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
