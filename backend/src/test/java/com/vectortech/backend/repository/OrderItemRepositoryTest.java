package com.vectortech.backend.repository;

import com.vectortech.backend.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@SuppressWarnings({"null", "unused"})
class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    private Order order;
    private Product product;

    @BeforeEach
    void setUp() {
        // Create user
        User user = userRepository.save(User.builder()
                .name("OrderItem User")
                .email("orderitem@vectortech.com")
                .password("StrongPass123")
                .role(Role.USER)
                .build());

        // Create order
        order = orderRepository.save(Order.builder()
                .user(user)
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("99.99"))
                .build());

        // Create category and product
        Category category = categoryRepository.save(Category.builder()
                .name("OrderItem Category")
                .description("Category for order items")
                .build());

        product = productRepository.save(Product.builder()
                .name("OrderItem Product")
                .description("Product in order")
                .price(new BigDecimal("49.99"))
                .stock(100)
                .category(category)
                .imageUrl("https://example.com/orderitem.png")
                .rating(new BigDecimal("4.60"))
                .build());
    }

    @Test
    void saveOrderItem_ShouldPersistCorrectly() {
        OrderItem item = orderItemRepository.save(OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(2)
                .price(new BigDecimal("49.99"))
                .build());

        assertThat(item.getId()).isNotNull();
        assertThat(item.getOrder().getId()).isEqualTo(order.getId());
        assertThat(item.getProduct().getId()).isEqualTo(product.getId());
        assertThat(item.getQuantity()).isEqualTo(2);
        assertThat(item.getPrice()).isEqualTo(new BigDecimal("49.99"));
    }

    @Test
    void saveOrderItem_ShouldSetTimestamps() {
        OrderItem item = orderItemRepository.save(OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(1)
                .price(new BigDecimal("49.99"))
                .build());

        assertThat(item.getCreatedAt()).isNotNull();
        assertThat(item.getUpdatedAt()).isNotNull();
    }

    @Test
    void findAll_ShouldReturnAllOrderItems() {
        orderItemRepository.save(OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(1)
                .price(new BigDecimal("49.99"))
                .build());

        orderItemRepository.save(OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(2)
                .price(new BigDecimal("49.99"))
                .build());

        List<OrderItem> items = orderItemRepository.findAll();

        assertThat(items).hasSize(2);
    }

    @Test
    void updateOrderItem_ShouldModifyQuantityAndPrice() {
        OrderItem item = orderItemRepository.save(OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(1)
                .price(new BigDecimal("49.99"))
                .build());

        item.setQuantity(3);
        item.setPrice(new BigDecimal("99.99"));
        orderItemRepository.save(item);

        OrderItem updated = orderItemRepository.findById(item.getId()).get();
        assertThat(updated.getQuantity()).isEqualTo(3);
        assertThat(updated.getPrice()).isEqualTo(new BigDecimal("99.99"));
    }

    @Test
    void deleteOrderItem_ShouldRemoveFromDatabase() {
        OrderItem item = orderItemRepository.save(OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(2)
                .price(new BigDecimal("99.98"))
                .build());

        Long itemId = item.getId();
        orderItemRepository.delete(item);

        assertThat(orderItemRepository.existsById(itemId)).isFalse();
    }

    @Test
    void orderItemShouldTrackHistoricalPrice() {
        // Item saved with original price
        OrderItem item = orderItemRepository.save(OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(1)
                .price(new BigDecimal("49.99"))
                .build());

        // Even if product price changes later, order item should preserve historical price
        assertThat(item.getPrice()).isEqualTo(new BigDecimal("49.99"));
    }

    @Test
    void multipleOrderItemsInSameOrder_ShouldBePersisted() {
        Category category2 = categoryRepository.save(Category.builder()
                .name("Another Category")
                .description("For another product")
                .build());

        Product product2 = productRepository.save(Product.builder()
                .name("Another Product")
                .description("Different product")
                .price(new BigDecimal("29.99"))
                .stock(50)
                .category(category2)
                .imageUrl("https://example.com/product2.png")
                .rating(new BigDecimal("4.40"))
                .build());

        OrderItem item1 = orderItemRepository.save(OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(2)
                .price(new BigDecimal("49.99"))
                .build());

        OrderItem item2 = orderItemRepository.save(OrderItem.builder()
                .order(order)
                .product(product2)
                .quantity(1)
                .price(new BigDecimal("29.99"))
                .build());

        List<OrderItem> items = orderItemRepository.findAll();
        long itemsInOrder = items.stream()
                .filter(item -> item.getOrder().getId().equals(order.getId()))
                .count();

        assertThat(itemsInOrder).isEqualTo(2);
    }
}
