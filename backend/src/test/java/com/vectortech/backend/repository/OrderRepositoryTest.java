package com.vectortech.backend.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.vectortech.backend.model.Category;
import com.vectortech.backend.model.Order;
import com.vectortech.backend.model.OrderItem;
import com.vectortech.backend.model.OrderStatus;
import com.vectortech.backend.model.Product;
import com.vectortech.backend.model.Role;
import com.vectortech.backend.model.User;

@DataJpaTest
@SuppressWarnings({"null", "unused"})
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        user = userRepository.save(User.builder()
                .name("Order User")
                .email("order.user@vectortech.com")
                .password("StrongPass123")
                .role(Role.USER)
                .build());

        Category category = categoryRepository.save(Category.builder()
                .name("Repository Test Category")
                .description("Repo category")
                .build());

        product = productRepository.save(Product.builder()
                .name("Order Test Product")
                .description("Order product")
                .price(new BigDecimal("49.99"))
                .stock(15)
                .category(category)
                .imageUrl("https://example.com/product.png")
                .rating(new BigDecimal("4.10"))
                .build());
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_ShouldReturnUserOrders() {
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("99.98"))
                .build();

        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(2)
                .price(new BigDecimal("49.99"))
                .build();

        order.getItems().add(item);
        orderRepository.save(order);

        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getItems()).hasSize(1);
        assertThat(orders.get(0).getItems().get(0).getProduct().getName()).isEqualTo("Order Test Product");
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_ShouldReturnEmptyForUserWithNoOrders() {
        User newUser = userRepository.save(User.builder()
                .name("No Orders User")
                .email("noorders@vectortech.com")
                .password("StrongPass123")
                .role(Role.USER)
                .build());

        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(newUser.getId());

        assertThat(orders).isEmpty();
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_ShouldSortByCreatedAtDescending() {
        Order order1 = orderRepository.save(Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .build());

        Order order2 = orderRepository.save(Order.builder()
                .user(user)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("75.00"))
                .build());

        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getCreatedAt())
                .isAfterOrEqualTo(orders.get(1).getCreatedAt());
    }

    @Test
    void findByIdAndUserId_ShouldReturnOrderForCorrectUser() {
        Order order = orderRepository.save(Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("49.99"))
                .build());

        Optional<Order> result = orderRepository.findByIdAndUserId(order.getId(), user.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(order.getId());
        assertThat(result.get().getUser().getId()).isEqualTo(user.getId());
    }

    @Test
    void findByIdAndUserId_ShouldReturnEmptyForDifferentUser() {
        Order saved = orderRepository.save(Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("49.99"))
                .build());

        User otherUser = userRepository.save(User.builder()
                .name("Other User")
                .email("other.user@vectortech.com")
                .password("StrongPass123")
                .role(Role.USER)
                .build());

        Optional<Order> result = orderRepository.findByIdAndUserId(saved.getId(), otherUser.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdAndUserId_ShouldReturnEmptyForInvalidOrderId() {
        Optional<Order> result = orderRepository.findByIdAndUserId(9999L, user.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void saveOrder_ShouldPersistAllFields() {
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.CONFIRMED)
                .totalAmount(new BigDecimal("199.99"))
                .build();

        Order saved = orderRepository.save(order);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUser().getId()).isEqualTo(user.getId());
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(saved.getTotalAmount()).isEqualTo(new BigDecimal("199.99"));
    }

    @Test
    void saveOrder_ShouldSetTimestamps() {
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("99.99"))
                .build();

        Order saved = orderRepository.save(order);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }

    @Test
    void saveOrder_ShouldDefaultStatusToPending() {
        Order order = Order.builder()
                .user(user)
                .totalAmount(new BigDecimal("99.99"))
                .build();

        Order saved = orderRepository.save(order);

        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    void updateOrder_ShouldModifyStatus() {
        Order order = orderRepository.save(Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("99.99"))
                .build());

        order.setStatus(OrderStatus.SHIPPED);
        orderRepository.save(order);

        Order updated = orderRepository.findById(order.getId()).get();
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }

    @Test
    void updateOrder_ShouldModifyTotalAmount() {
        Order order = orderRepository.save(Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("50.00"))
                .build());

        order.setTotalAmount(new BigDecimal("99.99"));
        orderRepository.save(order);

        Order updated = orderRepository.findById(order.getId()).get();
        assertThat(updated.getTotalAmount()).isEqualTo(new BigDecimal("99.99"));
    }

    @Test
    void deleteOrder_ShouldRemoveFromDatabase() {
        Order order = orderRepository.save(Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("99.99"))
                .build());

        Long orderId = order.getId();
        orderRepository.delete(order);

        assertThat(orderRepository.existsById(orderId)).isFalse();
    }

    @Test
    void orderWithMultipleItems_ShouldLoadAllItems() {
        Order order = orderRepository.save(Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("149.97"))
                .build());

        Category category = categoryRepository.save(Category.builder()
                .name("Multiple Items Category")
                .description("Category for testing")
                .build());

        Product product2 = productRepository.save(Product.builder()
                .name("Second Product")
                .description("Another product")
                .price(new BigDecimal("29.99"))
                .stock(10)
                .category(category)
                .build());

        OrderItem item1 = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(2)
                .price(new BigDecimal("49.99"))
                .build();

        OrderItem item2 = OrderItem.builder()
                .order(order)
                .product(product2)
                .quantity(1)
                .price(new BigDecimal("29.99"))
                .build();

        order.getItems().add(item1);
        order.getItems().add(item2);
        orderRepository.save(order);

        List<Order> found = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        assertThat(found).hasSize(1);
        assertThat(found.get(0).getItems()).hasSize(2);
    }

    @Test
    void findByUserIdOrderByCreatedAtDesc_ShouldLoadOrderItemsWithProducts() {
        Order order = orderRepository.save(Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("49.99"))
                .build());

        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(1)
                .price(new BigDecimal("49.99"))
                .build();

        order.getItems().add(item);
        orderRepository.save(order);

        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        assertThat(orders).hasSize(1);
        assertThat(orders.get(0).getItems()).hasSize(1);
        assertThat(orders.get(0).getItems().get(0).getProduct().getId()).isEqualTo(product.getId());
    }
}
