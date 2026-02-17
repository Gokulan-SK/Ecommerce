package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * CartItem Entity
 * Represents an item in a shopping cart
 */
@Entity
@Table(name = "cart_items", uniqueConstraints = @UniqueConstraint(name = "uq_cart_product", columnNames = { "cart_id",
                "product_id" }), indexes = {
                                @Index(name = "idx_cart_items_cart_id", columnList = "cart_id"),
                                @Index(name = "idx_cart_items_product_id", columnList = "product_id")
                })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "cart_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_items_cart"))
        @ToString.Exclude
        @EqualsAndHashCode.Exclude
        private Cart cart;

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "product_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_items_product"))
        private Product product;

        @Column(nullable = false)
        @Builder.Default
        private Integer quantity = 1;

        @Column(nullable = false, precision = 10, scale = 2)
        private BigDecimal unitPrice;

        @CreationTimestamp
        @Column(name = "created_at", nullable = false, updatable = false)
        private LocalDateTime createdAt;
}
