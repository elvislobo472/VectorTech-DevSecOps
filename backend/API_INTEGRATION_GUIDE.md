# VectorTech PostgreSQL Database - API Integration Guide

## Phase 2: API Integration & Service Layer

This guide shows how to integrate the PostgreSQL database with REST API endpoints.

---

## Quick Start

### 1. Verify Database Connection

```bash
# Start PostgreSQL
# Create 'vectortech' database
# Run Spring Boot application

mvn spring-boot:run
```

**Expected Log Output:**
```
HikariPool-1 - Starting pool...
HikariPool-1 - Added connection conn...
Hibernate: create table users...
Hibernate: create table categories...
Hibernate: create table products...
Hibernate: create table carts...
Hibernate: create table cart_items...
Hibernate: create table orders...
Hibernate: create table order_items...
```

### 2. Test Data Auto-Loaded

On startup, the `DataInitializer` creates:
- ✅ 4 Categories
- ✅ 6 Sample Products
- ✅ Test users with empty carts

### 3. Verify Installation

```bash
# Get all products (should return 6 products)
curl http://localhost:8080/api/products

# Get all categories (should return 4 categories)
curl http://localhost:8080/api/categories

# Search products
curl "http://localhost:8080/api/products/search?query=headphones"
```

---

## Database Schema Summary

| Table | Records Created | Purpose |
|---|---|---|
| users | 0 (manual) | Authentication & user profiles |
| categories | 4 | Product categorization |
| products | 6 | Catalog |
| carts | 0 (per user) | Shopping cart storage |
| cart_items | 0 (user items) | Items in cart |
| orders | 0 (per user) | Order history |
| order_items | 0 (order lines) | Items in orders |

---

## Example Service Layer Implementation

### ProductService (Example)

```java
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    
    // Get all products
    public List<ProductDTO> getAllProducts() {
        return productRepository.findAll()
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    // Get products by category
    public List<ProductDTO> getByCategory(String categoryName) {
        return productRepository.findByCategoryNameIgnoreCase(categoryName)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    // Search products
    public List<ProductDTO> search(String query) {
        return productRepository.searchProducts(query)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    // Get in-stock products
    public List<ProductDTO> getInStockProducts() {
        return productRepository.findByInStock(true)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    private ProductDTO toDTO(Product product) {
        return ProductDTO.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .price(product.getPrice())
            .stock(product.getStock())
            .imageUrl(product.getImageUrl())
            .categoryId(product.getCategory().getId())
            .categoryName(product.getCategory().getName())
            .rating(product.getRating())
            .inStock(product.isInStock())
            .createdAt(product.getCreatedAt())
            .build();
    }
}
```

### CartService (Example)

```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    
    // Get user's cart
    public CartDTO getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        return toDTO(cart);
    }
    
    // Add item to cart
    public CartDTO addItemToCart(Long userId, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Check if item already in cart
        CartItem existingItem = cart.getItems().stream()
            .filter(item -> item.getProduct().getId().equals(productId))
            .findFirst()
            .orElse(null);
        
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .build();
            cart.getItems().add(newItem);
        }
        
        cartRepository.save(cart);
        return toDTO(cart);
    }
    
    // Remove item from cart
    public CartDTO removeItemFromCart(Long userId, Long cartItemId) {
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        
        cart.getItems().removeIf(item -> item.getId().equals(cartItemId));
        cartRepository.save(cart);
        
        return toDTO(cart);
    }
    
    // Clear cart
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        cart.getItems().clear();
        cartRepository.save(cart);
    }
    
    private CartDTO toDTO(Cart cart) {
        return CartDTO.builder()
            .id(cart.getId())
            .userId(cart.getUser().getId())
            .items(cart.getItems().stream()
                .map(this::cartItemToDTO)
                .collect(Collectors.toList()))
            .totalPrice(calculateTotal(cart))
            .itemCount(cart.getItems().size())
            .createdAt(cart.getCreatedAt())
            .updatedAt(cart.getUpdatedAt())
            .build();
    }
    
    private CartItemDTO cartItemToDTO(CartItem item) {
        return CartItemDTO.builder()
            .id(item.getId())
            .productId(item.getProduct().getId())
            .productName(item.getProduct().getName())
            .price(item.getProduct().getPrice())
            .quantity(item.getQuantity())
            .subtotal(item.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity())))
            .build();
    }
    
    private BigDecimal calculateTotal(Cart cart) {
        return cart.getItems().stream()
            .map(item -> item.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
```

### OrderService (Example)

```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    
    // Create order from cart
    public OrderDTO createOrderFromCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cannot create order from empty cart");
        }
        
        // Calculate total
        BigDecimal totalAmount = cart.getItems().stream()
            .map(item -> item.getProduct().getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity())))
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Create order
        Order order = Order.builder()
            .user(cart.getUser())
            .status(OrderStatus.PENDING)
            .totalAmount(totalAmount)
            .build();
        
        // Create order items from cart items
        List<OrderItem> orderItems = cart.getItems().stream()
            .map(cartItem -> OrderItem.builder()
                .order(order)
                .product(cartItem.getProduct())
                .quantity(cartItem.getQuantity())
                .price(cartItem.getProduct().getPrice())
                .build())
            .collect(Collectors.toList());
        
        order.setItems(orderItems);
        Order savedOrder = orderRepository.save(order);
        
        // Clear cart after order creation
        cart.getItems().clear();
        cartRepository.save(cart);
        
        return toDTO(savedOrder);
    }
    
    // Get user's orders
    public List<OrderDTO> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
    }
    
    // Get specific order
    public OrderDTO getOrder(Long orderId, Long userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return toDTO(order);
    }
    
    // Update order status
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        order.setStatus(newStatus);
        orderRepository.save(order);
        return toDTO(order);
    }
    
    private OrderDTO toDTO(Order order) {
        return OrderDTO.builder()
            .id(order.getId())
            .userId(order.getUser().getId())
            .status(order.getStatus())
            .totalAmount(order.getTotalAmount())
            .items(order.getItems().stream()
                .map(this::orderItemToDTO)
                .collect(Collectors.toList()))
            .itemCount(order.getItems().size())
            .createdAt(order.getCreatedAt())
            .updatedAt(order.getUpdatedAt())
            .build();
    }
    
    private OrderItemDTO orderItemToDTO(OrderItem item) {
        return OrderItemDTO.builder()
            .id(item.getId())
            .productId(item.getProduct().getId())
            .productName(item.getProduct().getName())
            .quantity(item.getQuantity())
            .price(item.getPrice())
            .subtotal(item.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity())))
            .build();
    }
}
```

---

## API Endpoint Examples

### Products Endpoints

```
GET  /api/products               - Get all products
GET  /api/products/{id}          - Get product by ID
GET  /api/products/search        - Search products (query param)
GET  /api/products/category/{id} - Get products by category
GET  /api/products/instock       - Get in-stock products
POST /api/products               - Create product (admin)
PUT  /api/products/{id}          - Update product (admin)
DELETE /api/products/{id}        - Delete product (admin)
```

### Categories Endpoints

```
GET  /api/categories             - Get all categories
GET  /api/categories/{id}        - Get category by ID
POST /api/categories             - Create category (admin)
PUT  /api/categories/{id}        - Update category (admin)
DELETE /api/categories/{id}      - Delete category (admin)
```

### Cart Endpoints (Requires Authentication)

```
GET  /api/cart                   - Get current user's cart
POST /api/cart/items             - Add item to cart
DELETE /api/cart/items/{id}      - Remove item from cart
PUT  /api/cart/items/{id}        - Update cart item quantity
DELETE /api/cart                 - Clear cart
```

### Order Endpoints (Requires Authentication)

```
GET  /api/orders                 - Get user's orders
GET  /api/orders/{id}            - Get specific order
POST /api/orders                 - Create order from cart
PUT  /api/orders/{id}/status     - Update order status (admin)
```

---

## Testing with cURL

### Get All Products

```bash
curl -X GET http://localhost:8080/api/products \
  -H "Content-Type: application/json"
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "Premium Wireless Headphones",
    "description": "High-quality wireless headphones...",
    "price": 199.99,
    "stock": 50,
    "inStock": true,
    "rating": 4.80,
    "categoryName": "Electronics",
    "imageUrl": "https://images.unsplash.com/..."
  }
]
```

### Search Products

```bash
curl -X GET "http://localhost:8080/api/products/search?query=headphones" \
  -H "Content-Type: application/json"
```

### Get Products by Category

```bash
curl -X GET "http://localhost:8080/api/products/category/1" \
  -H "Content-Type: application/json"
```

### Create Order (Example)

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "cartId": 1
  }'
```

---

## Performance Optimization Tips

### 1. Database Indexing
Already implemented in `schema.sql`:
- ✅ `idx_product_category` - Fast category filtering
- ✅ `idx_product_name` - Fast product search
- ✅ `idx_orders_user_id` - Fast user order retrieval
- ✅ `idx_order_items_order_id` - Fast order line items
- ✅ `idx_cart_items_cart_id` - Fast cart item access

### 2. EntityGraph Usage
Reduces N+1 queries:
```java
@EntityGraph(attributePaths = {"user", "items", "items.product"})
Optional<Cart> findByUserId(Long userId);
```

### 3. Connection Pooling
HikariCP automatically manages connections (10 max, 5 min idle).

### 4. Query Optimization
Use repository methods instead of writing JPQL:
```java
// ✅ Good - Uses named query optimization
productRepository.findByCategoryId(categoryId)

// ❌ Avoid - Custom JPQL
repository.findAll() // then filter in Java
```

---

## Monitoring & Debugging

### Enable SQL Logging

Update `application.yml`:
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true

logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### Monitor Connection Pool

Spring Boot Actuator:
```bash
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics/hikaricp.connections
```

### Check Database Directly

```bash
# Connect to PostgreSQL
psql -U postgres -d vectortech

# List tables
\dt

# Check users
SELECT id, name, email, role FROM users;

# Check products
SELECT id, name, price, stock, in_stock FROM products;

# Check orders with items
SELECT o.id, o.status, COUNT(oi.id) as item_count, o.total_amount
FROM orders o
LEFT JOIN order_items oi ON o.id = oi.order_id
GROUP BY o.id;
```

---

## Common Issues & Solutions

### Issue: Database Connection Failed

**Solution:**
```bash
# Verify PostgreSQL is running
psql -U postgres

# Check if 'vectortech' database exists
\l

# Create if missing
CREATE DATABASE vectortech;
```

### Issue: "Unique constraint violation" on email

**Solution:**
```sql
-- Check duplicate emails
SELECT email, COUNT(*) FROM users GROUP BY email HAVING COUNT(*) > 1;

-- Delete duplicates (keep latest)
DELETE FROM users WHERE id NOT IN (
  SELECT MAX(id) FROM users GROUP BY email
);
```

### Issue: N+1 Query Performance

**Solution:**
Use EntityGraph in repository:
```java
@EntityGraph(attributePaths = {"items", "items.product"})
Optional<Cart> findByUserId(Long userId);
```

### Issue: Out of Memory with Cart Operations

**Solution:**
Use lazy loading for non-critical relationships:
```java
@ManyToOne(fetch = FetchType.LAZY)
private Category category;
```

---

## Migration to Production

Before deploying to production:

1. ✅ **Change ddl-auto:**
   ```yaml
   spring:
     jpa:
       hibernate:
         ddl-auto: validate  # Safety mode - validate only
   ```

2. ✅ **Use Flyway/Liquibase for migrations:**
   ```xml
   <dependency>
     <groupId>org.flywaydb</groupId>
     <artifactId>flyway-core</artifactId>
   </dependency>
   ```

3. ✅ **Backup database regularly:**
   ```bash
   pg_dump -U postgres vectortech > backup.sql
   ```

4. ✅ **Configure connection pooling for production:**
   ```yaml
   hikari:
     maximum-pool-size: 20
     minimum-idle: 5
   ```

5. ✅ **Enable query logging & monitoring:**
   ```yaml
   spring.jpa.show-sql: false  # Disable in production
   ```

---

## Deliverables Summary

✅ **PostgreSQL Database Schema** - Normalized design, 7 tables
✅ **JPA Entity Classes** - With validation annotations
✅ **Spring Data Repositories** - Custom queries, EntityGraph
✅ **Database Configuration** - HikariCP connection pooling
✅ **Data Seeding** - Auto-populated test data (4 categories, 6 products)
✅ **Unit Tests** - 72+ test cases covering all repositories
✅ **API Integration Guide** - Service layer examples, endpoint documentation
✅ **Performance Optimization** - Indexes, EntityGraph, lazy loading
✅ **Production Readiness** - Deployment checklist, monitoring

---

**Next Phase:** Service Layer & REST API Endpoints Implementation
