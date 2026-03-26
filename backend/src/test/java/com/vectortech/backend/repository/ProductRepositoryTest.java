package com.vectortech.backend.repository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.vectortech.backend.model.Category;
import com.vectortech.backend.model.Product;

@DataJpaTest
@SuppressWarnings({"null", "unused"})
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category electronics;

    @BeforeEach
    void setUp() {
        electronics = categoryRepository.save(Category.builder()
                .name("Electronics")
                .description("Electronic devices")
                .build());

        productRepository.save(Product.builder()
                .name("Vector Headphones")
                .description("Noise cancelling over-ear headphones")
                .price(new BigDecimal("199.99"))
                .stock(10)
                .category(electronics)
                .imageUrl("https://example.com/headphones.png")
                .rating(new BigDecimal("4.80"))
                .build());

        productRepository.save(Product.builder()
                .name("Vector Keyboard")
                .description("Mechanical keyboard")
                .price(new BigDecimal("129.99"))
                .stock(0)
                .category(electronics)
                .imageUrl("https://example.com/keyboard.png")
                .rating(new BigDecimal("4.50"))
                .build());
    }

    @Test
    void findByCategoryNameIgnoreCase_ShouldReturnProducts() {
        List<Product> products = productRepository.findByCategoryNameIgnoreCase("electronics");

        assertThat(products).hasSize(2);
    }

    @Test
    void findByCategoryNameIgnoreCase_ShouldBeCaseInsensitive() {
        List<Product> productsLower = productRepository.findByCategoryNameIgnoreCase("electronics");
        List<Product> productsUpper = productRepository.findByCategoryNameIgnoreCase("ELECTRONICS");
        List<Product> productsMixed = productRepository.findByCategoryNameIgnoreCase("ElEcTrOnIcs");

        assertThat(productsLower).hasSize(2);
        assertThat(productsUpper).hasSize(2);
        assertThat(productsMixed).hasSize(2);
    }

    @Test
    void findByCategoryNameIgnoreCase_ShouldReturnEmptyForNonexistent() {
        List<Product> products = productRepository.findByCategoryNameIgnoreCase("NonExistent");

        assertThat(products).isEmpty();
    }

    @Test
    void searchProducts_ShouldMatchNameAndDescription() {
        List<Product> nameMatches = productRepository.searchProducts("headphones");
        List<Product> descriptionMatches = productRepository.searchProducts("mechanical");

        assertThat(nameMatches).hasSize(1);
        assertThat(nameMatches.get(0).getName()).isEqualTo("Vector Headphones");
        assertThat(descriptionMatches).hasSize(1);
        assertThat(descriptionMatches.get(0).getName()).isEqualTo("Vector Keyboard");
    }

    @Test
    void searchProducts_ShouldBeCaseInsensitive() {
        List<Product> lowercase = productRepository.searchProducts("keyboard");
        List<Product> uppercase = productRepository.searchProducts("KEYBOARD");
        List<Product> mixed = productRepository.searchProducts("KeYbOaRd");

        assertThat(lowercase).hasSize(1);
        assertThat(uppercase).hasSize(1);
        assertThat(mixed).hasSize(1);
    }

    @Test
    void findByCategoryId_ShouldFilterByCategory() {
        List<Product> products = productRepository.findByCategoryId(electronics.getId());

        assertThat(products).hasSize(2);
    }

    @Test
    void findByCategoryId_ShouldReturnEmptyForNoProducts() {
        Category emptyCategory = categoryRepository.save(Category.builder()
                .name("Empty Category")
                .description("No products")
                .build());

        List<Product> products = productRepository.findByCategoryId(emptyCategory.getId());

        assertThat(products).isEmpty();
    }

    @Test
    void findByInStock_ShouldReturnOnlyInStockProducts() {
        List<Product> inStock = productRepository.findByInStock(true);

        assertThat(inStock).hasSize(1);
        assertThat(inStock.get(0).getName()).isEqualTo("Vector Headphones");
    }

    @Test
    void findByInStock_ShouldReturnOutOfStockProducts() {
        List<Product> outOfStock = productRepository.findByInStock(false);

        assertThat(outOfStock).hasSize(1);
        assertThat(outOfStock.get(0).getName()).isEqualTo("Vector Keyboard");
    }

    @Test
    void existsByNameIgnoreCase_ShouldReturnTrueWhenExists() {
        boolean exists = productRepository.existsByNameIgnoreCase("vector headphones");

        assertThat(exists).isTrue();
    }

    @Test
    void existsByNameIgnoreCase_ShouldBeCaseInsensitive() {
        boolean lowercase = productRepository.existsByNameIgnoreCase("vector keyboard");
        boolean uppercase = productRepository.existsByNameIgnoreCase("VECTOR KEYBOARD");
        boolean mixed = productRepository.existsByNameIgnoreCase("VeCtOr KeYbOaRd");

        assertThat(lowercase).isTrue();
        assertThat(uppercase).isTrue();
        assertThat(mixed).isTrue();
    }

    @Test
    void existsByNameIgnoreCase_ShouldReturnFalseWhenNotExists() {
        boolean exists = productRepository.existsByNameIgnoreCase("Nonexistent Product");

        assertThat(exists).isFalse();
    }

    @Test
    void saveProduct_ShouldPersistAllFields() {
        Product product = Product.builder()
                .name("New Product")
                .description("A brand new product")
                .price(new BigDecimal("79.99"))
                .stock(25)
                .category(electronics)
                .imageUrl("https://example.com/newproduct.png")
                .rating(new BigDecimal("4.20"))
                .build();

        Product saved = productRepository.save(product);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("New Product");
        assertThat(saved.getPrice()).isEqualTo(new BigDecimal("79.99"));
        assertThat(saved.getStock()).isEqualTo(25);
        assertThat(saved.getRating()).isEqualTo(new BigDecimal("4.20"));
    }

    @Test
    void saveProduct_ShouldSetTimestamps() {
        Product product = Product.builder()
                .name("Timestamp Product")
                .description("For timestamps")
                .price(new BigDecimal("49.99"))
                .stock(10)
                .category(electronics)
                .build();

        Product saved = productRepository.save(product);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void updateProduct_ShouldModifyPrice() {
        Product product = productRepository.findAll().get(0);
        BigDecimal originalPrice = product.getPrice();
        
        product.setPrice(new BigDecimal("149.99"));
        productRepository.save(product);

        Product updated = productRepository.findById(product.getId()).get();
        assertThat(updated.getPrice()).isNotEqualTo(originalPrice);
        assertThat(updated.getPrice()).isEqualTo(new BigDecimal("149.99"));
    }

    @Test
    void updateProduct_ShouldModifyStock() {
        Product product = productRepository.findAll().get(0);
        
        product.setStock(50);
        productRepository.save(product);

        Product updated = productRepository.findById(product.getId()).get();
        assertThat(updated.getStock()).isEqualTo(50);
        assertThat(updated.isInStock()).isTrue();
    }

    @Test
    void productInStock_ShouldUpdateWhenStockChanges() {
        Product product = productRepository.findAll().stream()
                .filter(p -> p.getStock() == 0)
                .findFirst()
                .get();

        assertThat(product.isInStock()).isFalse();

        product.setStock(5);
        productRepository.saveAndFlush(product);

        Product updated = productRepository.findById(product.getId()).get();
        assertThat(updated.isInStock()).isTrue();
    }

    @Test
    void deleteProduct_ShouldRemoveFromDatabase() {
        Product product = productRepository.findAll().get(0);
        Long productId = product.getId();

        productRepository.delete(product);

        assertThat(productRepository.existsById(productId)).isFalse();
    }
}
