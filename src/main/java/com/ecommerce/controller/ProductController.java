package com.ecommerce.controller;

import com.ecommerce.entity.Product;
import com.ecommerce.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for Product catalog operations.
 * Handles MVC requests for product listing and details.
 *
 * Extensions (non-destructive):
 *   - listProducts() now accepts ?category= param
 *   - New endpoint GET /products/flash-sale
 */
@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Display paginated product list with optional search, category filter, and sorting.
     * GET /products
     * GET /products?keyword=phone
     * GET /products?category=Electronics
     *
     * @param page      current page number (default 0)
     * @param size      page size (default 12)
     * @param sort      sort field (default name)
     * @param direction sort direction (default asc)
     * @param keyword   optional search keyword
     * @param category  optional category filter (matches category name)
     * @param model     Spring MVC model
     * @return products view name
     */
    @GetMapping
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "name") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            Model model) {

        // Build sort object
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc")
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));

        // Choose the right query: search > category > all
        Page<Product> productPage;
        if (keyword != null && !keyword.trim().isEmpty()) {
            productPage = productService.searchProducts(keyword, pageable);
        } else if (category != null && !category.trim().isEmpty()) {
            productPage = productService.getProductsByCategory(category, pageable);
        } else {
            productPage = productService.getAllProducts(pageable);
        }

        model.addAttribute("products",    productPage.getContent());
        model.addAttribute("currentPage", productPage.getNumber());
        model.addAttribute("totalPages",  productPage.getTotalPages());
        model.addAttribute("totalItems",  productPage.getTotalElements());
        model.addAttribute("sort",        sort);
        model.addAttribute("direction",   direction);
        model.addAttribute("keyword",     keyword);
        model.addAttribute("category",    category);
        model.addAttribute("categories",  productService.getAllCategories());

        return "products";
    }

    /**
     * Active flash sale product listing.
     * GET /products/flash-sale
     */
    @GetMapping("/flash-sale")
    public String flashSale(Model model) {
        model.addAttribute("flashProducts", productService.getActiveFlashSaleProducts());
        model.addAttribute("categories",    productService.getAllCategories());
        return "flash-sale";
    }

    /**
     * Display product detail page.
     * GET /products/{id}
     *
     * @param id    product ID
     * @param model Spring MVC model
     * @return product-detail view name
     */
    @GetMapping("/{id}")
    public String showProductDetail(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product-detail";
    }
}
