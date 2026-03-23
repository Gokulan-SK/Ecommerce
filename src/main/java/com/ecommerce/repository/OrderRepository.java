package com.ecommerce.repository;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.entity.User;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
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
     * Find all orders for a user by username, ordered by creation date descending
     * 
     * @param username the username
     * @return list of user's orders
     */
    List<Order> findByUserUsernameOrderByCreatedAtDesc(String username);

    /**
     * Find order by ID with items and products eagerly fetched
     * Prevents LazyInitializationException
     * 
     * @param id the order ID
     * @return Optional containing the order with items if found
     */
    @Query("""
                SELECT o FROM Order o
                LEFT JOIN FETCH o.items i
                LEFT JOIN FETCH i.product
                WHERE o.id = :id
            """)
    Optional<Order> findByIdWithItems(@Param("id") Long id);

    /**
     * Find all orders for a user with items and products eagerly fetched
     * Prevents LazyInitializationException
     * 
     * @param username the username
     * @return list of orders with items
     */
    @Query("""
                SELECT DISTINCT o FROM Order o
                LEFT JOIN FETCH o.items i
                LEFT JOIN FETCH i.product
                WHERE o.user.username = :username
                ORDER BY o.createdAt DESC
            """)
    List<Order> findByUserUsernameWithItems(@Param("username") String username);

    /**
     * Find order by ID with user isolation and items/products eagerly fetched
     * Prevents LazyInitializationException and unauthorized access
     * 
     * @param id       the order ID
     * @param username the username
     * @return Optional containing the order with items if found and belongs to user
     */
    @Query("""
                SELECT o FROM Order o
                LEFT JOIN FETCH o.items i
                LEFT JOIN FETCH i.product
                WHERE o.id = :id
                  AND o.user.username = :username
            """)
    Optional<Order> findDetailedOrder(@Param("id") Long id, @Param("username") String username);

    /**
     * Find order by ID and username (user isolation)
     * 
     * @param id       the order ID
     * @param username the username
     * @return Optional containing the order if found and belongs to user
     */
    Optional<Order> findByIdAndUserUsername(Long id, String username);

    /**
     * Find orders by status
     *
     * @param status the order status
     * @return list of orders with the specified status
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Find order by ID with PESSIMISTIC_WRITE lock.
     * Used exclusively by PaymentService to prevent concurrent payment on same
     * order.
     * Lock timeout: 3000ms — throws LockTimeoutException if cannot acquire.
     *
     * @param id the order ID
     * @return Optional containing the locked order if found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdForUpdate(@Param("id") Long id);

    @Query("""
                SELECT o FROM Order o
                JOIN FETCH o.user
                WHERE o.id = :id
                  AND o.user.username = :username
            """)
    Optional<Order> findForPayment(@Param("id") Long id,
            @Param("username") String username);

    @Query("""
                SELECT o FROM Order o
                LEFT JOIN FETCH o.items i
                LEFT JOIN FETCH i.product
                JOIN FETCH o.user u
                WHERE o.id = :id
                  AND u.username = :username
            """)
    Optional<Order> findDetailedForPayment(@Param("id") Long id,
            @Param("username") String username);
}
