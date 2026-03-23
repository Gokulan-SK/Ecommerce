package com.ecommerce.service;

import com.ecommerce.entity.Payment;
import com.ecommerce.entity.PaymentStatus;

/**
 * PaymentStateMachine
 *
 * Enforces ALL payment state transitions explicitly.
 * No implicit behavior — every transition is enumerated.
 * Any unlisted transition throws IllegalStateException.
 *
 * Allowed transitions:
 * PENDING → SUCCESS (payment succeeded)
 * PENDING → FAILED (payment declined)
 * PENDING → TIMEOUT (gateway timeout)
 * FAILED → SUCCESS (retry succeeded)
 * TIMEOUT → SUCCESS (retry succeeded)
 * SUCCESS → (none) TERMINAL — immutable forever
 *
 * Note: FAILED → FAILED and TIMEOUT → TIMEOUT are NOT allowed.
 * A new retry attempt resets to PENDING first (via gateway call),
 * then transitions to the gateway result.
 */
public final class PaymentStateMachine {

    private PaymentStateMachine() {
        // Utility class — not instantiable
    }

    /**
     * Applies a state transition to the given Payment.
     *
     * @param payment the payment to transition
     * @param next    the target state
     * @throws IllegalStateException if the transition is not allowed
     */
    public static void transition(Payment payment, PaymentStatus next) {
        PaymentStatus current = payment.getStatus();

        boolean allowed = switch (current) {
            // PENDING: can go to any outcome
            case PENDING -> next == PaymentStatus.SUCCESS
                    || next == PaymentStatus.FAILED
                    || next == PaymentStatus.TIMEOUT;

            // FAILED: retry — can only succeed (no FAILED→FAILED, no FAILED→TIMEOUT)
            case FAILED -> next == PaymentStatus.SUCCESS;

            // TIMEOUT: retry — can only succeed (no TIMEOUT→TIMEOUT, no TIMEOUT→FAILED)
            case TIMEOUT -> next == PaymentStatus.SUCCESS;

            // SUCCESS: terminal state — no transitions ever allowed
            case SUCCESS -> false;
        };

        if (!allowed) {
            throw new IllegalStateException(
                    String.format("Illegal Payment state transition: %s → %s. " +
                            "Allowed: PENDING→{SUCCESS,FAILED,TIMEOUT}, FAILED→SUCCESS, TIMEOUT→SUCCESS",
                            current, next));
        }

        payment.setStatus(next);
    }
}
