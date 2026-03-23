package com.ecommerce.service;

import com.ecommerce.entity.PaymentStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * FakePaymentGateway
 *
 * Pure simulation — no DB logic, no side effects.
 * Deterministic when chaos is disabled (always SUCCESS).
 * When chaos is enabled: 70% SUCCESS, 15% FAILED, 15% TIMEOUT.
 *
 * This class has no knowledge of Payment or Order entities.
 */
@Component
public class FakePaymentGateway {

    @Value("${payment.chaos.enabled:true}")
    private boolean chaosEnabled;

    /**
     * Simulates a payment gateway call.
     *
     * @param amount the payment amount (unused in simulation, present for API
     *               realism)
     * @return PaymentStatus result from the gateway
     */
    public PaymentStatus process(BigDecimal amount) {
        if (!chaosEnabled) {
            // Deterministic mode — always succeeds
            return PaymentStatus.SUCCESS;
        }

        // Chaos mode — probabilistic outcomes
        double roll = Math.random();

        if (roll < 0.70) {
            return PaymentStatus.SUCCESS; // 70%
        } else if (roll < 0.85) {
            return PaymentStatus.FAILED; // 15%
        } else {
            return PaymentStatus.TIMEOUT; // 15%
        }
    }
}
