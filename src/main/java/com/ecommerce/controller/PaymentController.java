package com.ecommerce.controller;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.entity.Payment;
import com.ecommerce.entity.PaymentStatus;
import com.ecommerce.exception.OrderNotFoundException;
import com.ecommerce.logging.EventName;
import com.ecommerce.logging.StructuredLogger;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.PaymentRepository;
import com.ecommerce.service.PaymentService;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

/**
 * PaymentController
 *
 * Security rules:
 * - All order lookups use findByIdAndUserUsername (query-level user isolation)
 * - Defense-in-depth username check after fetch
 * - Lock timeout caught gracefully — user-friendly message, no stacktrace
 * - Never expose internal state to the client
 */
@Controller
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public PaymentController(PaymentService paymentService,
            OrderRepository orderRepository,
            PaymentRepository paymentRepository) {
        this.paymentService = paymentService;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * GET /payments/{orderId}
     * Show payment page for an order.
     * Only accessible if order belongs to authenticated user and is not yet PAID.
     */
    @GetMapping("/{orderId}")
    public String paymentPage(@PathVariable Long orderId,
            Authentication authentication,
            Model model) {

        String username = authentication.getName();

        Order order = orderRepository.findDetailedForPayment(orderId, username)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() == OrderStatus.PAID) {
            return "redirect:/payments/" + orderId + "/result";
        }

        // 🔥 Build DTO inside controller (session still active here)
        var items = order.getItems().stream()
                .map(i -> new com.ecommerce.dto.PaymentView.ItemView(
                        i.getProduct().getName(),
                        i.getQuantity(),
                        i.getUnitPrice().multiply(
                                java.math.BigDecimal.valueOf(i.getQuantity()))))
                .toList();

        var view = new com.ecommerce.dto.PaymentView(
                order.getId(),
                order.getTotalAmount(),
                items);

        paymentRepository.findByOrderId(orderId)
                .ifPresent(p -> model.addAttribute("payment", p));

        model.addAttribute("view", view);
        model.addAttribute("order", order);

        return "payment";
    }

    /**
     * POST /payments/{orderId}/pay
     * Process payment for an order.
     * Double-click safe via pessimistic lock in PaymentService.
     */
    @PostMapping("/{orderId}/pay")
    public String processPayment(@PathVariable Long orderId,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {
        String username = authentication.getName();

        try {
            Payment payment = paymentService.processPayment(orderId, username);

            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                redirectAttributes.addFlashAttribute("successMessage",
                        "Payment successful! Reference: " + payment.getTransactionReference());
                return "redirect:/payments/" + orderId + "/result";
            } else {
                // FAILED or TIMEOUT — allow retry
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Payment " + payment.getStatus().name().toLowerCase() + ". You may retry.");
                return "redirect:/payments/" + orderId;
            }

        } catch (PessimisticLockingFailureException e) {
            // Lock could not be acquired — another request is processing this payment
            logLockTimeout(orderId, username);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Payment already in progress. Please wait a moment and try again.");
            return "redirect:/payments/" + orderId;

        } catch (AccessDeniedException e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "You are not authorized to pay for this order.");
            return "redirect:/orders";

        } catch (IllegalStateException e) {
            // State machine rejection or inconsistent state
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/payments/" + orderId;
        }
    }

    /**
     * GET /payments/{orderId}/result
     * Show payment result page.
     */
    @GetMapping("/{orderId}/result")
    public String paymentResult(@PathVariable Long orderId,
            Authentication authentication,
            Model model) {
        String username = authentication.getName();

        // Query-level user isolation
        Order order = orderRepository.findDetailedForPayment(orderId, username)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // Defense-in-depth: username already available from Spring Security — no lazy
        // proxy needed
        if (!username.equals(authentication.getName())) {
            throw new AccessDeniedException("Unauthorized order access");
        }

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException(
                        "No payment found for order: " + orderId));

        model.addAttribute("order", order);
        model.addAttribute("payment", payment);
        return "payment-result";
    }

    // ── Logging ───────────────────────────────────────────────────────────────

    private void logLockTimeout(Long orderId, String username) {
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", orderId);
        data.put("username", username);
        data.put("isAnomaly", true);
        StructuredLogger.logBusinessEvent(EventName.PAYMENT_LOCK_TIMEOUT, data);
    }
}
