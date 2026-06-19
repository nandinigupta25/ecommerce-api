package com.ecommerce.api.service;

import com.ecommerce.api.dto.request.ProductRequest;
import com.ecommerce.api.dto.response.PagedResponse;
import com.ecommerce.api.dto.response.ProductResponse;

import java.util.List;

/**
 * Service interface for product management operations.
 */
public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    ProductResponse updateProduct(Long id, ProductRequest request);

    void deleteProduct(Long id);

    ProductResponse getProductById(Long id);

    PagedResponse<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir);

    List<ProductResponse> getProductsByCategory(Long categoryId);

    PagedResponse<ProductResponse> searchProducts(String keyword, Long categoryId, int page, int size);
}
