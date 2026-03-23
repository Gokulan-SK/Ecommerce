package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.Payment;
import com.ecommerce.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Payment entity.
 * All queries are user-safe — callers must validate order ownership before use.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by order entity.
     * Returns empty if no payment exists for this order yet.
     */
    Optional<Payment> findByOrder(Order order);

    /**
     * Find payment by order ID.
     * Preferred when only the order ID is available.
     */
    Optional<Payment> findByOrderId(Long orderId);

    /**
     * Find payment by transaction reference.
     */
    Optional<Payment> findByTransactionReference(String transactionReference);

    /**
     * Find all payments with a given status.
     */
    List<Payment> findByStatus(PaymentStatus status);
}
