package com.ecommerce.service;

import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.logging.EventName;
import com.ecommerce.logging.StructuredLogger;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service layer for Product operations.
 * Handles business logic for product catalog management.
 *
 * New methods (non-destructive):
 *   - getProductsByCategory  → category-filtered page
 *   - getActiveFlashSaleProducts → flash sale items
 *   - getNewArrivals         → latest 8 products
 *   - getAllCategories        → all categories for nav/homepage
 */
@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    // ----------------------------------------------------------------
    // Existing methods — unchanged
    // ----------------------------------------------------------------

    /**
     * Get all products with pagination.
     */
    public Page<Product> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAllWithCategory(pageable);

        Map<String, Object> businessFields = new HashMap<>();
        businessFields.put("resultCount", products.getTotalElements());
        businessFields.put("pageNumber", pageable.getPageNumber());
        businessFields.put("pageSize", pageable.getPageSize());

        StructuredLogger.logBusinessEvent(EventName.PRODUCT_VIEW, businessFields);
        return products;
    }

    /**
     * Search products by keyword with pagination.
     */
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts(pageable);
        }

        String trimmedKeyword = keyword.trim();
        Page<Product> products = productRepository.searchWithCategory(trimmedKeyword, pageable);

        Map<String, Object> businessFields = new HashMap<>();
        businessFields.put("keyword", trimmedKeyword);
        businessFields.put("searchKeywordLength", trimmedKeyword.length());
        businessFields.put("resultCount", products.getTotalElements());
        businessFields.put("pageNumber", pageable.getPageNumber());

        StructuredLogger.logBusinessEvent(EventName.SEARCH_RESULT, businessFields);
        return products;
    }

    /**
     * Get product by ID.
     */
    public Product getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        Map<String, Object> businessFields = new HashMap<>();
        businessFields.put("productId", id);
        businessFields.put("productName", product.getName());
        businessFields.put("price", product.getPrice());
        businessFields.put("stock", product.getStock());

        StructuredLogger.logBusinessEvent(EventName.PRODUCT_VIEW, businessFields);
        return product;
    }

    // ----------------------------------------------------------------
    // New methods — non-destructive additions
    // ----------------------------------------------------------------

    /**
     * Get products filtered by category name with pagination.
     * Used by GET /products?category=Electronics
     */
    public Page<Product> getProductsByCategory(String categoryName, Pageable pageable) {
        Page<Product> products = productRepository.findByCategoryName(categoryName, pageable);

        Map<String, Object> businessFields = new HashMap<>();
        businessFields.put("category", categoryName);
        businessFields.put("resultCount", products.getTotalElements());
        businessFields.put("pageNumber", pageable.getPageNumber());

        StructuredLogger.logBusinessEvent(EventName.PRODUCT_VIEW, businessFields);
        return products;
    }

    /**
     * Return all products currently in an active flash sale.
     * A product is active if flashSale=true and now is within [flashStart, flashEnd).
     */
    public List<Product> getActiveFlashSaleProducts() {
        return productRepository.findActiveFlashSaleProducts(LocalDateTime.now());
    }

    /**
     * Return the 8 most recently added products for the "New Arrivals" section.
     */
    public List<Product> getNewArrivals() {
        return productRepository.findTop8WithCategoryByOrderByCreatedAtDesc();
    }

    /**
     * Return all categories for navbar dropdown and homepage grid.
     */
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
