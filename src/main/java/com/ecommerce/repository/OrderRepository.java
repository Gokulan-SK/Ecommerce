package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Order entity
 * Provides CRUD operations and custom queries for order management
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find all orders for a specific user, ordered by creation date descending
     * 
     * @param user the user
     * @return list of user's orders
     */
    List<Order> findByUserOrderByCreatedAtDesc(User user);

    /**
     * Find order by ID with payment details fetched
     * 
     * @param orderId the order ID
     * @return Optional containing the order with payment if found
     */
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.payment WHERE o.id = :orderId")
    Optional<Order> findByIdWithPayment(@Param("orderId") Long orderId);

    /**
     * Find orders by status
     * 
     * @param status the order status
     * @return list of orders with the specified status
     */
    List<Order> findByStatus(Order.OrderStatus status);
}
