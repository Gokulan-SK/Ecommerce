package com.ecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * HomeController
 * Serves the public landing page at /.
 */
@Controller
public class HomeController {

    /**
     * Landing page — publicly accessible.
     * GET /
     */
    @GetMapping("/")
    public String home() {
        return "index";
    }
}
