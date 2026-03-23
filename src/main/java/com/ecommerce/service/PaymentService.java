package com.ecommerce.service;

import com.ecommerce.entity.*;
import com.ecommerce.exception.OrderNotFoundException;
import com.ecommerce.logging.EventName;
import com.ecommerce.logging.StructuredLogger;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.PaymentRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * PaymentService — Production-grade payment processing.
 *
 * Guarantees:
 * - One payment row per order (DB UNIQUE + idempotency checks)
 * - All state transitions via OrderStateMachine / PaymentStateMachine
 * (explicit, no implicit)
 * - Pessimistic lock on Order before any payment logic
 * - Retry-safe: reuses existing FAILED/TIMEOUT payment row
 * - Idempotent: returns existing SUCCESS payment immediately
 * - All-or-nothing: single @Transactional
 */
@Service
public class PaymentService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final FakePaymentGateway fakePaymentGateway;

    public PaymentService(OrderRepository orderRepository,
            PaymentRepository paymentRepository,
            FakePaymentGateway fakePaymentGateway) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
        this.fakePaymentGateway = fakePaymentGateway;
    }

    /**
     * Process payment for an order.
     *
     * MANDATORY EXECUTION ORDER:
     * 1. Acquire PESSIMISTIC_WRITE lock on Order
     * 2. Validate order exists
     * 3. Validate order belongs to username (user isolation)
     * 4. Idempotency: if PAID, return existing SUCCESS payment
     * 5. Fetch existing payment (if any)
     * 6. Idempotency: if payment already SUCCESS, return it
     * 7. Log PAYMENT_INITIATED
     * 8. Get or create payment row (reuse FAILED/TIMEOUT, create if none)
     * 9. Call FakePaymentGateway
     * 10. Apply transitions via state machines (explicit — no implicit setStatus)
     * 11. Generate transactionReference only if SUCCESS and not already set
     * 12. Save payment and order atomically
     * 13. Log outcome
     *
     * @param orderId  the order to pay for
     * @param username the authenticated user
     * @return the Payment entity after processing
     * @throws OrderNotFoundException if order not found or not owned by user
     * @throws AccessDeniedException  if order belongs to another user
     * @throws IllegalStateException  if state machine rejects the transition
     */
    @Transactional
    public Payment processPayment(Long orderId, String username) {

        // ── STEP 1: Acquire PESSIMISTIC_WRITE lock on Order ──────────────────
        Order order = orderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        // ── STEP 2 & 3: Validate order belongs to this user ──────────────────
        if (!order.getUser().getUsername().equals(username)) {
            throw new AccessDeniedException(
                    "Order " + orderId + " does not belong to user: " + username);
        }

        // ── STEP 4: Idempotency — if order already PAID, return existing payment ──
        if (order.getStatus() == OrderStatus.PAID) {
            return paymentRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Inconsistent state: Order " + orderId + " is PAID but no payment row exists"));
        }

        // ── STEP 5: Fetch existing payment (if any) ───────────────────────────
        Optional<Payment> existingPayment = paymentRepository.findByOrderId(orderId);

        // ── STEP 6: Idempotency — if payment already SUCCESS, return it ───────
        if (existingPayment.isPresent()
                && existingPayment.get().getStatus() == PaymentStatus.SUCCESS) {
            return existingPayment.get();
        }

        // ── STEP 7: Log PAYMENT_INITIATED (before gateway call) ──────────────
        logPaymentInitiated(order, username);

        // ── STEP 8: Get or create payment row ─────────────────────────────────
        Payment payment;
        if (existingPayment.isEmpty()) {
            // First attempt — create new PENDING payment
            payment = Payment.builder()
                    .order(order)
                    .amount(order.getTotalAmount())
                    .status(PaymentStatus.PENDING)
                    .build();
            payment = paymentRepository.save(payment);
        } else {
            // Retry — reuse existing FAILED or TIMEOUT row (never create a second row)
            payment = existingPayment.get();
        }

        // ── STEP 9: Call FakePaymentGateway ───────────────────────────────────
        PaymentStatus gatewayResult = fakePaymentGateway.process(order.getTotalAmount());

        // ── STEP 10: Apply state transitions (EXPLICIT — no implicit setStatus) ──
        //
        // For retry cases (FAILED/TIMEOUT → SUCCESS), the state machine requires
        // transitioning through PENDING first is NOT needed — the machine allows
        // FAILED→SUCCESS and TIMEOUT→SUCCESS directly (retry path).
        PaymentStateMachine.transition(payment, gatewayResult);

        if (gatewayResult == PaymentStatus.SUCCESS) {
            // Order: CREATED→PAID or FAILED→PAID (retry success)
            OrderStateMachine.transition(order, OrderStatus.PAID);

            // ── STEP 11: Generate transactionReference ONLY if SUCCESS and not already set
            // ──
            if (payment.getTransactionReference() == null) {
                payment.setTransactionReference(UUID.randomUUID().toString());
            }

        } else {
            // FAILED or TIMEOUT:
            // Order: CREATED→FAILED or FAILED→FAILED (not allowed by state machine!)
            // OrderStateMachine enforces PAID→FAILED is illegal — throws if order is PAID
            // For FAILED→FAILED: state machine allows FAILED→PAID only, so we skip
            // the order transition if order is already FAILED (no-op needed)
            if (order.getStatus() != OrderStatus.FAILED) {
                OrderStateMachine.transition(order, OrderStatus.FAILED);
            }
            // If order is already FAILED, no transition needed (already in target state)
        }

        // ── STEP 12: Save atomically ──────────────────────────────────────────
        paymentRepository.save(payment);
        orderRepository.save(order);

        // ── STEP 13: Log outcome ──────────────────────────────────────────────
        if (gatewayResult == PaymentStatus.SUCCESS) {
            logPaymentSuccess(order, payment, username);
            logOrderMarkedPaid(order, payment, username);
        } else {
            logPaymentFailed(order, payment, username, gatewayResult);
        }

        return payment;
    }

    // ── Logging Helpers ───────────────────────────────────────────────────────

    private void logPaymentInitiated(Order order, String username) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", order.getUser().getId());
        data.put("username", username);
        data.put("orderId", order.getId());
        data.put("amount", order.getTotalAmount());
        data.put("orderStatus", order.getStatus().name());
        StructuredLogger.logBusinessEvent(EventName.PAYMENT_INITIATED, data);
    }

    private void logPaymentSuccess(Order order, Payment payment, String username) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", order.getUser().getId());
        data.put("username", username);
        data.put("orderId", order.getId());
        data.put("paymentId", payment.getId());
        data.put("amount", payment.getAmount());
        data.put("status", payment.getStatus().name());
        data.put("transactionReference", payment.getTransactionReference());
        StructuredLogger.logBusinessEvent(EventName.PAYMENT_SUCCESS, data);
    }

    private void logPaymentFailed(Order order, Payment payment, String username, PaymentStatus result) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", order.getUser().getId());
        data.put("username", username);
        data.put("orderId", order.getId());
        data.put("paymentId", payment.getId());
        data.put("amount", payment.getAmount());
        data.put("status", result.name());
        data.put("isAnomaly", true);
        StructuredLogger.logBusinessEvent(EventName.PAYMENT_FAILED, data);
    }

    private void logOrderMarkedPaid(Order order, Payment payment, String username) {
        Map<String, Object> data = new HashMap<>();
        data.put("userId", order.getUser().getId());
        data.put("username", username);
        data.put("orderId", order.getId());
        data.put("paymentId", payment.getId());
        data.put("amount", payment.getAmount());
        data.put("status", OrderStatus.PAID.name());
        StructuredLogger.logBusinessEvent(EventName.ORDER_MARKED_PAID, data);
    }
}
