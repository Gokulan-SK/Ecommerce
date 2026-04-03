package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity.
 * Provides CRUD operations and custom queries for product management.
 *
 * New queries (non-destructive additions):
 *   - findByCategoryName              → category filter with pagination
 *   - findActiveFlashSaleProducts     → currently active flash sale items
 *   - findTop8ByOrderByCreatedAtDesc  → new arrivals for homepage
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Get all products with pagination — eagerly fetches category to avoid LazyInitializationException.
     * Uses a separate countQuery so Hibernate doesn't try to do JOIN FETCH in the count.
     */
    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category",
           countQuery = "SELECT count(p) FROM Product p")
    Page<Product> findAllWithCategory(Pageable pageable);

    /**
     * Search by name or description — eagerly fetches category.
     */
    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category "
                 + "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) "
                 + "   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))",
           countQuery = "SELECT count(p) FROM Product p "
                      + "WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) "
                      + "   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchWithCategory(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find products by name or description keyword — returns list.
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Find products with stock greater than zero.
     */
    @Query("SELECT p FROM Product p WHERE p.stock > 0")
    List<Product> findAvailableProducts();

    /**
     * Find products by exact name (for chaos testing).
     */
    List<Product> findByName(String name);

    /**
     * Find product by ID with pessimistic write lock for stock updates.
     * Prevents concurrent stock modifications during checkout.
     * Lock timeout: 3000ms.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);

    // ----------------------------------------------------------------
    // New queries — non-destructive additions
    // ----------------------------------------------------------------

    /**
     * Filter products by category name — eagerly fetches category.
     */
    @Query(value = "SELECT p FROM Product p LEFT JOIN FETCH p.category c WHERE LOWER(c.name) = LOWER(:categoryName)",
           countQuery = "SELECT count(p) FROM Product p JOIN p.category c WHERE LOWER(c.name) = LOWER(:categoryName)")
    Page<Product> findByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);

    /**
     * Return products currently in an active flash sale — eagerly fetches category.
     */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.flashSale = true AND p.flashStart <= :now AND p.flashEnd > :now ORDER BY p.discountedPrice ASC")
    List<Product> findActiveFlashSaleProducts(@Param("now") LocalDateTime now);

    /**
     * Return the 8 most recently inserted products — eagerly fetches category.
     */
    @Query("SELECT p FROM Product p LEFT JOIN FETCH p.category ORDER BY p.createdAt DESC LIMIT 8")
    List<Product> findTop8WithCategoryByOrderByCreatedAtDesc();
}
