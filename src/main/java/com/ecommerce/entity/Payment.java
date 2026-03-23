package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Payment Entity
 * One payment row per order (enforced by UNIQUE constraint on order_id in
 * schema.sql).
 *
 * Rules:
 * - @ManyToOne(optional=false) → Order (as required)
 * - No cascade to Order
 * - No eager loading (OIV=false safe)
 * - transactionReference only set on SUCCESS, never regenerated
 * - Status transitions enforced exclusively by PaymentStateMachine
 */
@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_payments_order_id", columnList = "order_id"),
        @Index(name = "idx_payments_status", columnList = "status"),
        @Index(name = "idx_payments_transaction_ref", columnList = "transaction_reference")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * ManyToOne (not OneToOne) as specified — no cascade to Order.
     * UNIQUE on order_id enforced at DB level in schema.sql.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payments_order"))
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Order order;

    /**
     * Precision 12,2 — matches Order.totalAmount exactly.
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /**
     * Status managed exclusively by PaymentStateMachine.
     * Default PENDING set at construction time.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * Unique transaction reference.
     * Null until status == SUCCESS.
     * Never regenerated once set.
     */
    @Column(name = "transaction_reference", unique = true, length = 100)
    private String transactionReference;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
