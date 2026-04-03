package com.ecommerce.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * AdminController
 * Provides the admin dashboard entry point.
 * All routes under /admin are restricted to ROLE_ADMIN by SecurityConfig.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    public String adminDashboard() {
        return "admin/index";
    }
}
