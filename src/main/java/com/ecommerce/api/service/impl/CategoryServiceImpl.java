package com.ecommerce.api.service.impl;

import com.ecommerce.api.dto.request.CategoryRequest;
import com.ecommerce.api.dto.response.CategoryResponse;
import com.ecommerce.api.entity.Category;
import com.ecommerce.api.exception.BadRequestException;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.CategoryRepository;
import com.ecommerce.api.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of CategoryService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category with name '" + request.getName() + "' already exists");
        }

        Category category = Category.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .build();

        category = categoryRepository.save(category);
        log.info("Category created: {}", category.getName());
        return mapToResponse(category);
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = findById(id);

        // If name is changing, ensure the new name isn't already taken
        if (!category.getName().equals(request.getName()) &&
                categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category name '" + request.getName() + "' is already in use");
        }

        category.setName(request.getName());
        if (request.getDescription() != null) category.setDescription(request.getDescription());
        if (request.getImageUrl() != null)    category.setImageUrl(request.getImageUrl());

        category = categoryRepository.save(category);
        log.info("Category updated: id={}", id);
        return mapToResponse(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = findById(id);
        if (!category.getProducts().isEmpty()) {
            throw new BadRequestException(
                    "Cannot delete category '" + category.getName() + "' — it has associated products");
        }
        categoryRepository.delete(category);
        log.info("Category deleted: id={}", id);
    }

    @Override
    public CategoryResponse getCategoryById(Long id) {
        return mapToResponse(findById(id));
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---- Private helpers ----

    private Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    private CategoryResponse mapToResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .productCount(category.getProducts().size())
                .createdAt(category.getCreatedAt())
                .build();
    }
}
