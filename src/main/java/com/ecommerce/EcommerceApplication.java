package com.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Main Spring Boot Application Class
 * E-Commerce Log Generator for ML-based Anomaly Detection
 * 
 * This application serves as a controlled log generator with:
 * - Structured ECS JSON logging
 * - Chaos engineering capabilities
 * - Comprehensive business flow simulation
 * 
 * @author Senior Java Architect
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAspectJAutoProxy
public class EcommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }

}
