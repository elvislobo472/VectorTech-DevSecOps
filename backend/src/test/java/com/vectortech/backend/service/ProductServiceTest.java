package com.vectortech.backend.service;

import com.vectortech.backend.dto.ProductDto;
import com.vectortech.backend.exception.ResourceNotFoundException;
import com.vectortech.backend.model.Category;
import com.vectortech.backend.model.Product;
import com.vectortech.backend.repository.CategoryRepository;
import com.vectortech.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
                .id(1L)
                .name("Electronics")
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Headphones")
                .description("A great product")
                .price(new BigDecimal("199.99"))
                .category(testCategory)
                .stock(10)
                .inStock(true)
                .rating(new BigDecimal("4.5"))
                .build();
    }

    @Test
    void getAllProducts_ShouldReturnListOfProductDtos() {
        when(productRepository.findAll()).thenReturn(List.of(testProduct));

        List<ProductDto> result = productService.getAllProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Headphones");
        assertThat(result.get(0).getPrice()).isEqualTo(new BigDecimal("199.99"));
        assertThat(result.get(0).getCategory()).isEqualTo("Electronics");
    }

    @Test
    void getProductById_WithValidId_ShouldReturnProductDto() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        ProductDto result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Headphones");
    }

    @Test
    void getProductById_WithInvalidId_ShouldThrowResourceNotFoundException() {
        when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void searchProducts_ShouldReturnMatchingProducts() {
        when(productRepository.searchProducts("headphone")).thenReturn(List.of(testProduct));

        List<ProductDto> result = productService.searchProducts("headphone");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).contains("Headphones");
    }

    @Test
    void getProductsByCategory_ShouldReturnFilteredProducts() {
        when(productRepository.findByCategoryNameIgnoreCase("Electronics")).thenReturn(List.of(testProduct));

        List<ProductDto> result = productService.getProductsByCategory("Electronics");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("Electronics");
    }

    @Test
    void deleteProduct_WithInvalidId_ShouldThrowResourceNotFoundException() {
        when(productRepository.existsById(anyLong())).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).deleteById(anyLong());
    }
}
