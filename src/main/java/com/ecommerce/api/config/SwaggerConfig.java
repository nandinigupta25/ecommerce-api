package com.ecommerce.api.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger / OpenAPI 3 configuration.
 *
 * Access Swagger UI at: http://localhost:8080/api/swagger-ui.html
 * Access raw docs at:   http://localhost:8080/api/v3/api-docs
 */
@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Paste your JWT token here (without the 'Bearer ' prefix)"
)
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("E-Commerce REST API")
                        .version("1.0.0")
                        .description("""
                                Production-ready E-Commerce Backend API built with:
                                - **Java 21** + **Spring Boot 3.2**
                                - **Spring Security** with **JWT** authentication
                                - **MySQL** + **Spring Data JPA**
                                - **Role-based access control** (ADMIN / CUSTOMER)
                                
                                ### Quick Start
                                1. Register a new account via `POST /api/auth/register`
                                2. Login via `POST /api/auth/login` to get your JWT token
                                3. Click **Authorize** above and paste the token
                                4. Explore the protected endpoints
                                
                                ### Default Credentials (from seed data)
                                - **Admin** → username: `admin`, password: `Admin@1234`
                                - **Customer** → username: `johndoe`, password: `Customer@1234`
                                """)
                        .contact(new Contact()
                                .name("E-Commerce API")
                                .email("admin@ecommerce.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8080/api").description("Local Development"),
                        new Server().url("https://api.ecommerce.com").description("Production")
                ));
    }
}
