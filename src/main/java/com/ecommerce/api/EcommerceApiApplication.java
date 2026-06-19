package com.ecommerce.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the E-Commerce REST API application.
 *
 * Tech Stack:
 * - Java 21
 * - Spring Boot 3.2
 * - Spring Security + JWT
 * - Spring Data JPA + Hibernate
 * - MySQL
 * - Swagger / OpenAPI
 */
@SpringBootApplication
public class EcommerceApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(EcommerceApiApplication.class, args);
    }
}
