package com.ecommerce.service;

import com.ecommerce.entity.Product;
import com.ecommerce.exception.ProductNotFoundException;
import com.ecommerce.logging.EventName;
import com.ecommerce.logging.StructuredLogger;
import com.ecommerce.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Service layer for Product operations
 * Handles business logic for product catalog management
 */
@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Get all products with pagination
     * 
     * @param pageable pagination information
     * @return page of products
     */
    public Page<Product> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);

        // Log business event
        Map<String, Object> businessFields = new HashMap<>();
        businessFields.put("resultCount", products.getTotalElements());
        businessFields.put("pageNumber", pageable.getPageNumber());
        businessFields.put("pageSize", pageable.getPageSize());

        StructuredLogger.logBusinessEvent(EventName.PRODUCT_VIEW, businessFields);

        return products;
    }

    /**
     * Search products by keyword with pagination
     * 
     * @param keyword  search keyword
     * @param pageable pagination information
     * @return page of matching products
     */
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts(pageable);
        }

        String trimmedKeyword = keyword.trim();
        Page<Product> products = productRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        trimmedKeyword,
                        trimmedKeyword,
                        pageable);

        // Log search event
        Map<String, Object> businessFields = new HashMap<>();
        businessFields.put("keyword", trimmedKeyword);
        businessFields.put("searchKeywordLength", trimmedKeyword.length());
        businessFields.put("resultCount", products.getTotalElements());
        businessFields.put("pageNumber", pageable.getPageNumber());

        StructuredLogger.logBusinessEvent(EventName.SEARCH_RESULT, businessFields);

        return products;
    }

    /**
     * Get product by ID
     * 
     * @param id product ID
     * @return product
     * @throws ProductNotFoundException if product not found
     */
    public Product getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        // Log product view event
        Map<String, Object> businessFields = new HashMap<>();
        businessFields.put("productId", id);
        businessFields.put("productName", product.getName());
        businessFields.put("price", product.getPrice());
        businessFields.put("stock", product.getStock());

        StructuredLogger.logBusinessEvent(EventName.PRODUCT_VIEW, businessFields);

        return product;
    }
}
