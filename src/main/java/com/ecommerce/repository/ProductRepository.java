package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Product entity
 * Provides CRUD operations and custom queries for product management
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

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
}
