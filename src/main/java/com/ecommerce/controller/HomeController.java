package com.ecommerce.controller;

import com.ecommerce.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * HomeController
 * Serves the public landing page at /.
 * Now enriched with categories, flash sales, and new arrivals for the marketplace homepage.
 */
@Controller
public class HomeController {

    private final ProductService productService;

    public HomeController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * Landing page — publicly accessible.
     * GET /
     * Adds to model:
     *   - categories    → all categories for the browse grid
     *   - flashProducts → currently active flash sale products
     *   - newArrivals   → 8 most recently added products
     */
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories",    productService.getAllCategories());
        model.addAttribute("flashProducts", productService.getActiveFlashSaleProducts());
        model.addAttribute("newArrivals",   productService.getNewArrivals());
        return "index";
    }
}
