package com.vectortech.backend.repository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.vectortech.backend.model.Cart;
import com.vectortech.backend.model.CartItem;
import com.vectortech.backend.model.Category;
import com.vectortech.backend.model.Product;
import com.vectortech.backend.model.Role;
import com.vectortech.backend.model.User;

@DataJpaTest
@SuppressWarnings({"null", "unused"})
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User user;
    private Cart cart;
    private Product product;
        private Product product2;

    @BeforeEach
    void setUp() {
        // Create user
        user = userRepository.save(User.builder()
                .name("Cart Test User")
                .email("cart.user@vectortech.com")
                .password("StrongPass123")
                .role(Role.USER)
                .build());

        // Create cart
        cart = cartRepository.save(Cart.builder()
                .user(user)
                .build());

        // Create category and product
        Category category = categoryRepository.save(Category.builder()
                .name("Cart Test Category")
                .description("Category for testing cart")
                .build());

        product = productRepository.save(Product.builder()
                .name("Cart Test Product")
                .description("Product for testing")
                .price(new BigDecimal("29.99"))
                .stock(50)
                .category(category)
                .imageUrl("https://example.com/product.png")
                .rating(new BigDecimal("4.30"))
                .build());

        product2 = productRepository.save(Product.builder()
                .name("Cart Test Product 2")
                .description("Second product for testing")
                .price(new BigDecimal("19.99"))
                .stock(25)
                .category(category)
                .imageUrl("https://example.com/product2.png")
                .rating(new BigDecimal("4.10"))
                .build());
    }

    @Test
    void findByUserId_ShouldReturnCartWithUser() {
        Optional<Cart> found = cartRepository.findByUserId(user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUser().getId()).isEqualTo(user.getId());
        assertThat(found.get().getUser().getEmail()).isEqualTo("cart.user@vectortech.com");
    }

    @Test
    void findByUserId_ShouldReturnEmptyWhenUserHasNoCart() {
        User newUser = userRepository.save(User.builder()
                .name("No Cart User")
                .email("nocart@vectortech.com")
                .password("StrongPass123")
                .role(Role.USER)
                .build());

        Optional<Cart> found = cartRepository.findByUserId(newUser.getId());

        assertThat(found).isEmpty();
    }

    @Test
    void findByUserId_ShouldLoadCartItems() {
        // Add items to cart
        cartItemRepository.save(CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(2)
                .build());

        cartItemRepository.save(CartItem.builder()
                .cart(cart)
                .product(product2)
                .quantity(1)
                .build());

        cartItemRepository.flush();
        entityManager.clear();

        Optional<Cart> found = cartRepository.findByUserId(user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getItems()).hasSize(2);
        assertThat(found.get().getItems()).extracting("quantity").containsExactly(2, 1);
    }

    @Test
    void findGraphById_ShouldLoadRelations() {
        cartItemRepository.save(CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(3)
                .build());

        cartItemRepository.flush();
        entityManager.clear();

        Optional<Cart> found = cartRepository.findGraphById(cart.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUser()).isNotNull();
        assertThat(found.get().getUser().getName()).isEqualTo("Cart Test User");
        assertThat(found.get().getItems()).hasSize(1);
        assertThat(found.get().getItems().get(0).getProduct().getName()).isEqualTo("Cart Test Product");
    }

    @Test
    void saveCart_ShouldPersistTimestamps() {
        User newUser = userRepository.save(User.builder()
                .name("New Cart User")
                .email("newcart@vectortech.com")
                .password("StrongPass123")
                .role(Role.USER)
                .build());

        Cart newCart = cartRepository.save(Cart.builder()
                .user(newUser)
                .build());

        assertThat(newCart.getCreatedAt()).isNotNull();
        assertThat(newCart.getUpdatedAt()).isNotNull();
        assertThat(newCart.getCreatedAt()).isEqualTo(newCart.getUpdatedAt());
    }

    @Test
    void cart_ShouldMaintainOneToOneRelationshipWithUser() {
        // Verify user has reference to cart
        Optional<Cart> foundCart = cartRepository.findByUserId(user.getId());

        assertThat(foundCart).isPresent();
        assertThat(foundCart.get().getUser().getId()).isEqualTo(user.getId());
    }
}
