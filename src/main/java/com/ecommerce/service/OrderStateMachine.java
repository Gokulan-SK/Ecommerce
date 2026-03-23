package com.ecommerce.service;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;

/**
 * OrderStateMachine
 *
 * Enforces ALL order state transitions explicitly.
 * No implicit behavior — every transition is enumerated.
 * Any unlisted transition throws IllegalStateException.
 *
 * Allowed transitions:
 * CREATED → PAID (checkout success)
 * CREATED → FAILED (checkout/payment failure)
 * FAILED → PAID (retry success)
 * PAID → (none) TERMINAL — immutable forever
 */
public final class OrderStateMachine {

    private OrderStateMachine() {
        // Utility class — not instantiable
    }

    /**
     * Applies a state transition to the given Order.
     *
     * @param order the order to transition
     * @param next  the target state
     * @throws IllegalStateException if the transition is not allowed
     */
    public static void transition(Order order, OrderStatus next) {
        OrderStatus current = order.getStatus();

        boolean allowed = switch (current) {
            // CREATED: can go to PAID (success) or FAILED (failure)
            case CREATED -> next == OrderStatus.PAID || next == OrderStatus.FAILED;

            // FAILED: can only go to PAID (retry success) — cannot go back to CREATED
            case FAILED -> next == OrderStatus.PAID;

            // PAID: terminal state — no transitions ever allowed
            case PAID -> false;
        };

        if (!allowed) {
            throw new IllegalStateException(
                    String.format("Illegal Order state transition: %s → %s. " +
                            "Allowed: CREATED→PAID, CREATED→FAILED, FAILED→PAID",
                            current, next));
        }

        order.setStatus(next);
    }
}
