package com.ecommerce.repository;

import com.ecommerce.entity.Cart;
import com.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Cart entity
 * Provides CRUD operations and custom queries for cart management
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find active cart for a user
     * 
     * @param user   the user
     * @param status the cart status
     * @return Optional containing the active cart if found
     */
    Optional<Cart> findByUserAndStatus(User user, Cart.CartStatus status);

    /**
     * Find active cart by user ID with items fetched
     * 
     * @param userId the user ID
     * @return Optional containing the active cart with items
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.user.id = :userId AND c.status = 'ACTIVE'")
    Optional<Cart> findActiveCartWithItems(@Param("userId") Long userId);
}
