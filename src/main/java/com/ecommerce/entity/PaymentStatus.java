package com.ecommerce.entity;

/**
 * PaymentStatus Enum
 * Represents all possible states of a payment.
 *
 * State Machine:
 * PENDING → SUCCESS
 * PENDING → FAILED
 * PENDING → TIMEOUT
 * FAILED → SUCCESS (retry allowed)
 * TIMEOUT → SUCCESS (retry allowed)
 * SUCCESS → (terminal — no transitions allowed)
 */
public enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
    TIMEOUT
}
