package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Category Entity
 * Groups products into browsable sections.
 * Backward compatible — all existing products work without a category (nullable FK).
 */
@Entity
@Table(name = "categories",
        indexes = @Index(name = "idx_categories_name", columnList = "name"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    /** Emoji or short icon string for display in UI. */
    @Column(length = 10)
    private String icon;
}
