package com.ecommerce.controller;

import com.ecommerce.entity.Order;
import com.ecommerce.exception.OrderNotFoundException;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.service.OrderService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * OrderController
 * Handles checkout and order viewing
 */
@Controller
@RequestMapping("/orders")
@Transactional(readOnly = true)
public class OrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepository;

    public OrderController(OrderService orderService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    /**
     * Checkout - Convert cart to order
     * POST /orders/checkout
     */
    @PostMapping("/checkout")
    @Transactional
    public String checkout(Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();

            // Call service to create order (service handles logging)
            Order order = orderService.checkout(username);

            // Success - redirect to order confirmation
            redirectAttributes.addFlashAttribute("successMessage",
                    "Order placed successfully! Order #" + order.getId());
            return "redirect:/orders/" + order.getId();

        } catch (Exception e) {
            // Error - redirect back to cart
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/cart";
        }
    }

    /**
     * View order confirmation
     * GET /orders/{id}
     */
    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Authentication authentication, Model model) {
        String username = authentication.getName();

        // Use user-isolated query with JOIN FETCH
        Order order = orderRepository.findDetailedOrder(id, username)
                .orElseThrow(() -> new OrderNotFoundException(id));

        // Defense-in-depth: use Spring Security principal — avoids lazy proxy access
        // outside session
        if (!username.equals(authentication.getName())) {
            throw new AccessDeniedException("Unauthorized order access");
        }

        model.addAttribute("order", order);
        return "order-confirmation";
    }

    /**
     * View order history
     * GET /orders
     */
    @GetMapping
    public String orderHistory(Authentication authentication, Model model) {
        String username = authentication.getName();

        // Use JOIN FETCH to prevent LazyInitializationException
        List<Order> orders = orderRepository.findByUserUsernameWithItems(username);

        model.addAttribute("orders", orders);
        return "order-history";
    }
}
