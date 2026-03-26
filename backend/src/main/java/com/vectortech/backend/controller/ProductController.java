package com.vectortech.backend.controller;

import com.vectortech.backend.dto.ProductDto;
import com.vectortech.backend.dto.ProductRequest;
import com.vectortech.backend.service.ProductService;
import com.vectortech.backend.util.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * GET /api/products
     * Returns all products. Public.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDto>>> getAllProducts() {
        return ResponseEntity.ok(ApiResponse.success(productService.getAllProducts()));
    }

    /**
     * GET /api/products/{id}
     * Returns a product by ID. Public.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDto>> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductById(id)));
    }

    /**
     * GET /api/products/search?query=
     * Search products by name/description. Public.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDto>>> searchProducts(
            @RequestParam String query) {
        return ResponseEntity.ok(ApiResponse.success(productService.searchProducts(query)));
    }

    /**
     * GET /api/products/category/{category}
     * Filter products by category name. Public.
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ProductDto>>> getProductsByCategory(
            @PathVariable String category) {
        return ResponseEntity.ok(ApiResponse.success(productService.getProductsByCategory(category)));
    }

    /**
     * POST /api/products
     * Create a new product. ADMIN only.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDto>> createProduct(
            @Valid @RequestBody ProductRequest request) {
        ProductDto product = productService.createProduct(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Product created", product));
    }

    /**
     * PUT /api/products/{id}
     * Update an existing product. ADMIN only.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductDto>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request) {
        ProductDto product = productService.updateProduct(id, request);
        return ResponseEntity.ok(ApiResponse.success("Product updated", product));
    }

    /**
     * DELETE /api/products/{id}
     * Delete a product. ADMIN only.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Product deleted", null));
    }
}
