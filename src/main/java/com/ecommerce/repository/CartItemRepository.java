package com.ecommerce.repository;

import com.ecommerce.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for CartItem entity
 * Provides CRUD operations for cart items
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Standard CRUD operations inherited from JpaRepository
    // Custom queries can be added here if needed
}
