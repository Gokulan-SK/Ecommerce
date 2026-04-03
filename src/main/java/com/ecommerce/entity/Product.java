package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Product Entity
 * Represents a product in the e-commerce catalog.
 *
 * New optional fields (all backward compatible, nullable):
 *   - category      → ManyToOne to Category (nullable)
 *   - flashSale     → is this product on flash sale?
 *   - discountedPrice → sale price (null if not on sale)
 *   - flashStart/flashEnd → active window for the sale
 */
@Entity
@Table(name = "products", indexes = {
        @Index(name = "idx_products_name", columnList = "name"),
        @Index(name = "idx_products_price", columnList = "price"),
        @Index(name = "idx_products_category", columnList = "category_id"),
        @Index(name = "idx_products_flash_sale", columnList = "flash_sale")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;

    @Version
    @Column(name = "version")
    private Long version;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // ----------------------------------------------------------------
    // New optional fields — all backward compatible
    // ----------------------------------------------------------------

    /** Category this product belongs to (nullable). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    private Category category;

    /** Whether this product is participating in a flash sale. */
    @Column(name = "flash_sale", nullable = false)
    @Builder.Default
    private boolean flashSale = false;

    /** Sale price. Meaningful only when flashSale = true and time window is active. */
    @Column(name = "discounted_price", precision = 10, scale = 2)
    private BigDecimal discountedPrice;

    /** Flash sale start time (inclusive). */
    @Column(name = "flash_start")
    private LocalDateTime flashStart;

    /** Flash sale end time (exclusive). */
    @Column(name = "flash_end")
    private LocalDateTime flashEnd;

    // ----------------------------------------------------------------
    // Derived helper — safe to call in Thymeleaf templates
    // ----------------------------------------------------------------

    /**
     * Returns true if the product is currently in an active flash sale.
     * Checks the time window server-side so templates stay simple.
     */
    public boolean isFlashSaleActive() {
        if (!flashSale || discountedPrice == null) return false;
        LocalDateTime now = LocalDateTime.now();
        return flashStart != null && flashEnd != null
                && !now.isBefore(flashStart)
                && now.isBefore(flashEnd);
    }

    /**
     * The effective display price: discounted price when flash sale is active,
     * otherwise the regular price.
     */
    public BigDecimal getEffectivePrice() {
        return isFlashSaleActive() ? discountedPrice : price;
    }

    /**
     * Hours remaining in the flash sale (0 if not active or expired).
     */
    public long getFlashSaleHoursLeft() {
        if (!isFlashSaleActive()) return 0;
        return java.time.Duration.between(LocalDateTime.now(), flashEnd).toHours();
    }
}
