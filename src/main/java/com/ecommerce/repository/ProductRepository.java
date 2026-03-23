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

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Product entity
 * Provides CRUD operations and custom queries for product management
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find products by name or description containing search keyword
     * (case-insensitive) with pagination
     * 
     * @param name        the search keyword for name
     * @param description the search keyword for description
     * @param pageable    the pagination information
     * @return page of matching products
     */
    Page<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
            String name,
            String description,
            Pageable pageable);

    /**
     * Find products by name containing search keyword (case-insensitive)
     * 
     * @param keyword the search keyword
     * @return list of matching products
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchByKeyword(@Param("keyword") String keyword);

    /**
     * Find products with stock greater than zero
     * 
     * @return list of available products
     */
    @Query("SELECT p FROM Product p WHERE p.stock > 0")
    List<Product> findAvailableProducts();

    /**
     * Find products by exact name (for chaos testing)
     * 
     * @param name the exact product name
     * @return list of matching products
     */
    List<Product> findByName(String name);

    /**
     * Find product by ID with pessimistic write lock for stock updates
     * Prevents concurrent stock modifications during checkout
     * 
     * @param id the product ID
     * @return Optional containing the locked product
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
