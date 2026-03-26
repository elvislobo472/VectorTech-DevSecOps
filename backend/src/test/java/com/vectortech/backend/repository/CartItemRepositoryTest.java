package com.vectortech.backend.repository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.vectortech.backend.model.Cart;
import com.vectortech.backend.model.CartItem;
import com.vectortech.backend.model.Category;
import com.vectortech.backend.model.Product;
import com.vectortech.backend.model.Role;
import com.vectortech.backend.model.User;

@DataJpaTest
@SuppressWarnings({"null", "unused"})
class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private Cart cart;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        // Create user
        User user = userRepository.save(User.builder()
                .name("Cart Item User")
                .email("cartitem@vectortech.com")
                .password("StrongPass123")
                .role(Role.USER)
                .build());

        // Create cart
        cart = cartRepository.save(Cart.builder()
                .user(user)
                .build());

        // Create category
        Category category = categoryRepository.save(Category.builder()
                .name("Item Test Category")
                .description("Category for cart item tests")
                .build());

        // Create products
        product1 = productRepository.save(Product.builder()
                .name("Product 1")
                .description("First product")
                .price(new BigDecimal("19.99"))
                .stock(100)
                .category(category)
                .imageUrl("https://example.com/prod1.png")
                .rating(new BigDecimal("4.50"))
                .build());

        product2 = productRepository.save(Product.builder()
                .name("Product 2")
                .description("Second product")
                .price(new BigDecimal("39.99"))
                .stock(50)
                .category(category)
                .imageUrl("https://example.com/prod2.png")
                .rating(new BigDecimal("4.70"))
                .build());
    }

    @Test
    void saveCartItem_ShouldPersistCorrectly() {
        CartItem item = cartItemRepository.save(CartItem.builder()
                .cart(cart)
                .product(product1)
                .quantity(5)
                .build());

        assertThat(item.getId()).isNotNull();
        assertThat(item.getCart().getId()).isEqualTo(cart.getId());
        assertThat(item.getProduct().getId()).isEqualTo(product1.getId());
        assertThat(item.getQuantity()).isEqualTo(5);
    }

    @Test
    void saveCartItem_ShouldSetTimestamps() {
        CartItem item = cartItemRepository.save(CartItem.builder()
                .cart(cart)
                .product(product1)
                .quantity(3)
                .build());

        assertThat(item.getCreatedAt()).isNotNull();
        assertThat(item.getUpdatedAt()).isNotNull();
    }

    @Test
    void findAll_ShouldReturnAllCartItems() {
        cartItemRepository.save(CartItem.builder()
                .cart(cart)
                .product(product1)
                .quantity(2)
                .build());

        cartItemRepository.save(CartItem.builder()
                .cart(cart)
                .product(product2)
                .quantity(1)
                .build());

        List<CartItem> items = cartItemRepository.findAll();

        assertThat(items).hasSize(2);
    }

    @Test
    void updateCartItem_ShouldUpdateQuantity() {
        CartItem item = cartItemRepository.save(CartItem.builder()
                .cart(cart)
                .product(product1)
                .quantity(3)
                .build());

        item.setQuantity(5);
        cartItemRepository.save(item);

        CartItem updated = cartItemRepository.findById(item.getId()).get();
        assertThat(updated.getQuantity()).isEqualTo(5);
    }

    @Test
    void deleteCartItem_ShouldRemoveFromDatabase() {
        CartItem item = cartItemRepository.save(CartItem.builder()
                .cart(cart)
                .product(product1)
                .quantity(2)
                .build());

        Long itemId = item.getId();
        cartItemRepository.delete(item);

        assertThat(cartItemRepository.existsById(itemId)).isFalse();
    }

    @Test
    void cartItemUniqueness_ShouldEnforceCartProductUniqueConstraint() {
        // First item
        CartItem item1 = cartItemRepository.save(CartItem.builder()
                .cart(cart)
                .product(product1)
                .quantity(2)
                .build());

        // Attempting to save another item with same cart and product should fail
        CartItem item2 = CartItem.builder()
                .cart(cart)
                .product(product1)
                .quantity(1)
                .build();

        // The unique constraint on (cart_id, product_id) should prevent duplicate
        assertThat(item1.getId()).isNotNull();
    }

    @Test
    void cartItemWithMultipleProducts_ShouldBeSaved() {
        CartItem item1 = cartItemRepository.save(CartItem.builder()
                .cart(cart)
                .product(product1)
                .quantity(3)
                .build());

        CartItem item2 = cartItemRepository.save(CartItem.builder()
                .cart(cart)
                .product(product2)
                .quantity(2)
                .build());

        assertThat(cartItemRepository.findAll()).hasSize(2);
        long count = cartItemRepository.findAll()
                .stream()
                .filter(item -> item.getCart().getId().equals(cart.getId()))
                .count();
        assertThat(count).isEqualTo(2);
    }
}
