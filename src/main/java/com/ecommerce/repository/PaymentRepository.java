package com.ecommerce.repository;

import com.ecommerce.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payment entity
 * Provides CRUD operations and custom queries for payment management
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * Find payment by order ID
     * 
     * @param orderId the order ID
     * @return Optional containing the payment if found
     */
    Optional<Payment> findByOrderId(Long orderId);

    /**
     * Find payment by transaction reference
     * 
     * @param transactionReference the transaction reference
     * @return Optional containing the payment if found
     */
    Optional<Payment> findByTransactionReference(String transactionReference);

    /**
     * Find payments by status
     * 
     * @param status the payment status
     * @return list of payments with the specified status
     */
    List<Payment> findByStatus(Payment.PaymentStatus status);
}
