package com.ecommerce.controller;

import com.ecommerce.entity.Cart;
import com.ecommerce.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller for shopping cart operations
 * Handles MVC requests for cart management
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    /**
     * Display the shopping cart
     * GET /cart
     */
    @GetMapping
    public String viewCart(Authentication authentication, Model model) {
        String username = authentication.getName();
        Cart cart = cartService.getCart(username);

        model.addAttribute("cart", cart);
        return "cart";
    }

    /**
     * Add a product to the cart
     * POST /cart/add/{productId}
     */
    @PostMapping("/add/{productId}")
    public String addToCart(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {
            String username = authentication.getName();
            cartService.addProductToCart(username, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Product added to cart successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        // Redirect back to referring page or fallback to /products
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isEmpty()) {
            return "redirect:" + referer;
        }
        return "redirect:/products";
    }

    /**
     * Remove a product from the cart
     * POST /cart/remove/{productId}
     */
    @PostMapping("/remove/{productId}")
    public String removeFromCart(
            @PathVariable Long productId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String username = authentication.getName();
            cartService.removeProductFromCart(username, productId);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Product removed from cart!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }

    /**
     * Update quantity of a product in the cart
     * POST /cart/update/{productId}
     */
    @PostMapping("/update/{productId}")
    public String updateQuantity(
            @PathVariable Long productId,
            @RequestParam int quantity,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            String username = authentication.getName();
            cartService.updateQuantity(username, productId, quantity);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Cart updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/cart";
    }
}
