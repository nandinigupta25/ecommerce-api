package com.ecommerce.api.service;

import com.ecommerce.api.dto.request.CategoryRequest;
import com.ecommerce.api.dto.response.CategoryResponse;

import java.util.List;

/**
 * Service interface for product category operations.
 */
public interface CategoryService {

    CategoryResponse createCategory(CategoryRequest request);

    CategoryResponse updateCategory(Long id, CategoryRequest request);

    void deleteCategory(Long id);

    CategoryResponse getCategoryById(Long id);

    List<CategoryResponse> getAllCategories();
}
