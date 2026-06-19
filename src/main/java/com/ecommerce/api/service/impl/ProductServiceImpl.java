package com.ecommerce.api.service.impl;

import com.ecommerce.api.dto.request.ProductRequest;
import com.ecommerce.api.dto.response.PagedResponse;
import com.ecommerce.api.dto.response.ProductResponse;
import com.ecommerce.api.entity.Category;
import com.ecommerce.api.entity.Product;
import com.ecommerce.api.exception.ResourceNotFoundException;
import com.ecommerce.api.repository.CategoryRepository;
import com.ecommerce.api.repository.ProductRepository;
import com.ecommerce.api.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of ProductService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = findCategory(request.getCategoryId());

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .category(category)
                .active(true)
                .build();

        product = productRepository.save(product);
        log.info("Product created: {} (id={})", product.getName(), product.getId());
        return mapToResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = findProduct(id);
        Category category = findCategory(request.getCategoryId());

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setCategory(category);
        if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());

        product = productRepository.save(product);
        log.info("Product updated: id={}", id);
        return mapToResponse(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = findProduct(id);
        // Soft delete — mark as inactive instead of removing from DB
        product.setActive(false);
        productRepository.save(product);
        log.info("Product soft-deleted: id={}", id);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        return mapToResponse(findProduct(id));
    }

    @Override
    public PagedResponse<ProductResponse> getAllProducts(int page, int size,
                                                         String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Product> productPage = productRepository.findByActiveTrue(pageable);

        return buildPagedResponse(productPage);
    }

    @Override
    public List<ProductResponse> getProductsByCategory(Long categoryId) {
        // Validate category exists first
        findCategory(categoryId);
        return productRepository.findByCategoryIdAndActiveTrue(categoryId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PagedResponse<ProductResponse> searchProducts(String keyword, Long categoryId,
                                                          int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productPage;

        if (categoryId != null) {
            productPage = productRepository.searchProductsByCategory(keyword, categoryId, pageable);
        } else {
            productPage = productRepository.searchProducts(keyword, pageable);
        }

        return buildPagedResponse(productPage);
    }

    // ---- Private helpers ----

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));
    }

    private PagedResponse<ProductResponse> buildPagedResponse(Page<Product> page) {
        List<ProductResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PagedResponse.<ProductResponse>builder()
                .content(content)
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .first(page.isFirst())
                .build();
    }

    /**
     * Maps a Product entity to a ProductResponse DTO.
     */
    public ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .active(product.isActive())
                .categoryId(product.getCategory().getId())
                .categoryName(product.getCategory().getName())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
