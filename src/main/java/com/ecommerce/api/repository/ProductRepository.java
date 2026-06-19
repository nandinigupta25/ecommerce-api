package com.ecommerce.api.repository;

import com.ecommerce.api.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /** Get all active products with pagination */
    Page<Product> findByActiveTrue(Pageable pageable);

    /** Get all active products by category */
    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);

    /**
     * Full-text search across product name and description.
     * Case-insensitive LIKE query.
     */
    @Query("SELECT p FROM Product p WHERE p.active = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);

    /** Search by keyword within a specific category */
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.category.id = :categoryId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProductsByCategory(@Param("keyword") String keyword,
                                           @Param("categoryId") Long categoryId,
                                           Pageable pageable);
}
