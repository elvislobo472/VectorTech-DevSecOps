package com.vectortech.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vectortech.backend.dto.ProductDto;
import com.vectortech.backend.dto.ProductRequest;
import com.vectortech.backend.exception.ResourceNotFoundException;
import com.vectortech.backend.model.Category;
import com.vectortech.backend.model.Product;
import com.vectortech.backend.repository.CategoryRepository;
import com.vectortech.backend.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return toDto(product);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> searchProducts(String query) {
        return productRepository.searchProducts(query)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getProductsByCategory(String category) {
        return productRepository.findByCategoryNameIgnoreCase(category)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public ProductDto createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .category(category)
                .imageUrl(request.getImageUrl())
                .stock(request.getStock() != null ? request.getStock() : Integer.valueOf(0))
                .build();

        Product saved = productRepository.save(product);
        log.info("Product created: {}", saved.getId());
        return toDto(saved);
    }

    @Transactional
    public ProductDto updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setCategory(category);
        product.setImageUrl(request.getImageUrl());
        if (request.getStock() != null) {
            product.setStock(request.getStock());
        }

        Product updated = productRepository.save(product);
        log.info("Product updated: {}", updated.getId());
        return toDto(updated);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", "id", id);
        }
        productRepository.deleteById(id);
        log.info("Product deleted: {}", id);
    }

    private ProductDto toDto(Product product) {
        return ProductDto.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .category(product.getCategory() != null ? product.getCategory().getName() : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .imageUrl(product.getImageUrl())
                .stock(product.getStock())
                .rating(product.getRating())
                .inStock(product.isInStock())
                .createdAt(product.getCreatedAt())
                .build();
    }
}
