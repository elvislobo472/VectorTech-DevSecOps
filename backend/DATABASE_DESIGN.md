# VectorTech PostgreSQL Database Design & Integration

## Phase 2: Database Architecture Documentation

This document provides comprehensive details about the PostgreSQL database design for the VectorTech e-commerce platform.

---

## Table of Contents

1. [Database Overview](#database-overview)
2. [Schema Design](#schema-design)
3. [Entity Relationships](#entity-relationships)
4. [JPA Entity Classes](#jpa-entity-classes)
5. [Repository Interfaces](#repository-interfaces)
6. [Configuration](#configuration)
7. [Data Seeding](#data-seeding)
8. [API Response Examples](#api-response-examples)
9. [Best Practices](#best-practices)
10. [Testing](#testing)

---

## Database Overview

**Technology Stack:**
- **Database:** PostgreSQL 12+
- **ORM:** Spring Data JPA / Hibernate
- **Connection Pool:** HikariCP
- **Spring Boot Version:** 3.2.3
- **Java Version:** 17

**Key Features:**
- Normalized schema design (3NF)
- ACID compliance
- Referential integrity with foreign keys
- Indexes for query optimization
- Audit timestamps (createdAt, updatedAt)
- Cascade operations for data consistency

---

## Schema Design

### Normalized Tables (7 Core Tables)

```
┌─────────────────┐
│     USERS       │ (Authentication & User Management)
├─────────────────┤
│ id (PK)         │
│ name            │
│ email (UNIQUE)  │
│ password        │
│ role (ENUM)     │
│ created_at      │
│ updated_at      │
└─────────────────┘
        │
        ├──────────    ┌──────────────────┐
        │              │    CARTS         │ (One-to-One)
        │              ├──────────────────┤
        │              │ id (PK)          │
        │              │ user_id (FK)     │
        │              │ created_at       │
        │              │ updated_at       │
        │              └──────────────────┘
        │                      │
        │                      ├────────    ┌─────────────────┐
        │                      │            │  CART_ITEMS     │
        │                      │            ├─────────────────┤
        │                      └─────────────│ cart_id (FK)    │
        │                                   │ product_id (FK) │
        │                                   │ quantity        │
        │                                   └─────────────────┘
        │
        └──────────    ┌──────────────────┐
                       │    ORDERS        │ (One-to-Many)
                       ├──────────────────┤
                       │ id (PK)          │
                       │ user_id (FK)     │
                       │ status (ENUM)    │
                       │ total_amount     │
                       │ created_at       │
                       │ updated_at       │
                       └──────────────────┘
                              │
                              └────────    ┌──────────────────┐
                                           │  ORDER_ITEMS     │
                                           ├──────────────────┤
                                           │ order_id (FK)    │
                                           │ product_id (FK)  │
                                           │ quantity         │
                                           │ price            │
                                           └──────────────────┘

┌──────────────────┐
│   CATEGORIES     │
├──────────────────┤
│ id (PK)          │
│ name (UNIQUE)    │
│ description      │
│ created_at       │
│ updated_at       │
└──────────────────┘
        │
        └────────────────────────┐
                                  │
                    ┌─────────────────────┐
                    │     PRODUCTS        │
                    ├─────────────────────┤
                    │ id (PK)             │
                    │ name                │
                    │ description         │
                    │ price               │
                    │ stock               │
                    │ image_url           │
                    │ rating              │
                    │ in_stock (boolean)  │
                    │ category_id (FK)    │
                    │ created_at          │
                    │ updated_at          │
                    └─────────────────────┘
                           ▲
                           │
              (Referenced by CART_ITEMS & ORDER_ITEMS)
```

### SQL Table Definitions

All table definitions with constraints and indexes are provided in:
**File:** `backend/src/main/resources/schema.sql`

Key features:
- Primary Keys: Auto-incrementing BIGSERIAL
- Foreign Keys: Referential integrity with CASCADE/RESTRICT
- Check Constraints: Numeric range validations
- Unique Constraints: Email uniqueness, category name uniqueness
- Indexes: Optimized for common queries
- Default Values: CURRENT_TIMESTAMP for audit columns

---

## Entity Relationships

### Relationship Types

| Relationship | From | To | Cardinality | Cascade |
|---|---|---|---|---|
| User → Orders | User | Order | One-to-Many | REMOVE on user delete |
| User → Cart | User | Cart | One-to-One | CASCADE (auto-create) |
| Cart → CartItems | Cart | CartItem | One-to-Many | CASCADE (orphan removal) |
| Products → CartItems | Product | CartItem | Many-to-One | LAZY (no cascade) |
| Products → OrderItems | Product | OrderItem | Many-to-One | LAZY (no cascade) |
| Categories → Products | Category | Product | One-to-Many | RESTRICT (prevent deletion) |

### Fetch Strategies

- **User Details:** LAZY (fetch only when accessed)
- **Cart & Items:** Uses EntityGraph for eager loading
- **Orders & Items:** Uses EntityGraph with product details
- **Product Relationships:** LAZY to avoid N+1 queries

---

## JPA Entity Classes

All entities are located in: `backend/src/main/java/com/vectortech/backend/model/`

### User Entity
```java
@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User implements UserDetails {
    Long id
    String name
    String email (unique)
    String password
    Role role (ENUM: USER, ADMIN)
    List<Order> orders (One-to-Many)
    Cart cart (One-to-One)
    LocalDateTime createdAt
    LocalDateTime updatedAt
}
```

**Validation Annotations:**
- `@NotBlank` on name, email, password
- `@Email` on email
- `@Size(min=8, max=255)` on password
- `@Size(max=100)` on name

### Category Entity
```java
@Entity
@Table(name = "categories", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Category {
    Long id
    String name (unique)
    String description (TEXT)
    List<Product> products (One-to-Many)
    LocalDateTime createdAt
    LocalDateTime updatedAt
}
```

### Product Entity
```java
@Entity
@Table(name = "products")
public class Product {
    Long id
    String name
    String description (TEXT)
    BigDecimal price
    Integer stock
    String imageUrl
    Category category (Many-to-One)
    BigDecimal rating
    boolean inStock
    LocalDateTime createdAt
    LocalDateTime updatedAt
}
```

**Validation Annotations:**
- `@NotBlank` on name
- `@NotNull` on price, stock, category
- `@DecimalMin("0.0")` on price
- `@Min(0)` on stock

**Special Features:**
- `inStock` flag automatically updated based on stock level
- Rating constrained to 0-5 range
- Price with precision 10,2 (decimal places = 2)

### Cart & CartItem Entities
```java
@Entity
@Table(name = "carts")
public class Cart {
    Long id
    User user (One-to-One)
    List<CartItem> items (One-to-Many with orphan removal)
    LocalDateTime createdAt
    LocalDateTime updatedAt
}

@Entity
@Table(name = "cart_items")
public class CartItem {
    Long id
    Cart cart (Many-to-One)
    Product product (Many-to-One)
    Integer quantity
    LocalDateTime createdAt
    LocalDateTime updatedAt
}
```

**Constraints:**
- Unique constraint on (cart_id, product_id) - prevent duplicates
- Quantity minimum 1

### Order & OrderItem Entities
```java
@Entity
@Table(name = "orders")
public class Order {
    Long id
    User user (Many-to-One)
    OrderStatus status (ENUM)
    BigDecimal totalAmount
    List<OrderItem> items (One-to-Many with orphan removal)
    LocalDateTime createdAt
    LocalDateTime updatedAt
}

@Entity
@Table(name = "order_items")
public class OrderItem {
    Long id
    Order order (Many-to-One)
    Product product (Many-to-One)
    Integer quantity
    BigDecimal price (Historical price at order time)
    LocalDateTime createdAt
    LocalDateTime updatedAt
}
```

**Order Status Enum:**
```java
public enum OrderStatus {
    PENDING,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
```

---

## Repository Interfaces

All repositories extend `JpaRepository<Entity, ID>` and are located in:
`backend/src/main/java/com/vectortech/backend/repository/`

### UserRepository
```java
Optional<User> findByEmail(String email);
boolean existsByEmail(String email);
```

### ProductRepository
```java
List<Product> findByCategoryNameIgnoreCase(String categoryName);
List<Product> searchProducts(@Param("query") String query);
List<Product> findByInStock(boolean inStock);
List<Product> findByCategoryId(Long categoryId);
boolean existsByNameIgnoreCase(String name);
```

### OrderRepository
```java
@EntityGraph(attributePaths = {"user", "items", "items.product"})
List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);

@EntityGraph(attributePaths = {"user", "items", "items.product"})
Optional<Order> findByIdAndUserId(Long orderId, Long userId);
```

### CartRepository
```java
@EntityGraph(attributePaths = {"user", "items", "items.product"})
Optional<Cart> findByUserId(Long userId);

@EntityGraph(attributePaths = {"user", "items", "items.product"})
Optional<Cart> findGraphById(@Param("id") Long id);
```

### CategoryRepository
```java
Optional<Category> findByName(String name);
boolean existsByName(String name);
```

---

## Configuration

### Database Connection (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vectortech
    username: postgres
    password: your_password_here
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      idle-timeout: 600000           # 10 minutes
      connection-timeout: 30000      # 30 seconds
      max-lifetime: 1800000         # 30 minutes

  jpa:
    hibernate:
      ddl-auto: update              # Auto-update schema
    show-sql: true
    open-in-view: false             # Lazy loading in view disabled
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
```

### HikariCP Connection Pool Configuration

**Default Values:**
- **Maximum Connections:** 10
- **Minimum Idle:** 5
- **Connection Timeout:** 30 seconds
- **Idle Timeout:** 10 minutes
- **Max Lifetime:** 30 minutes

**Performance Tuning:**
- Adjust `maximum-pool-size` based on concurrent users
- Reduce if database connections are limited
- Monitor with Spring Boot Actuator endpoints

---

## Data Seeding

### DataInitializer Component

**File:** `backend/src/main/java/com/vectortech/backend/config/DataInitializer.java`

Automatically executes on application startup via `CommandLineRunner`.

**Seeded Data:**

#### Categories
1. Electronics
2. Wearables
3. Accessories
4. Storage

#### Sample Products (6 Products)
- Premium Wireless Headphones ($199.99) - Electronics, 50 stock
- Smartwatch Pro ($299.99) - Wearables, 30 stock
- 4K Webcam ($149.99) - Electronics, 40 stock
- Mechanical Keyboard ($129.99) - Accessories, 60 stock
- Portable SSD 1TB ($89.99) - Storage, 75 stock
- USB-C Hub ($49.99) - Accessories, 0 stock (out of stock)

**Upsert Logic:**
- Uses `existsByName()` to prevent duplicates
- Safe for repeated application startups
- Empty cart automatically created for test users

**Custom Setup:**
To customize seed data, modify the methods in `DataInitializer`:
```java
upsertCategory(String name, String description)
upsertProduct(String name, String description, BigDecimal price, 
              Integer stock, String imageUrl, Category category, BigDecimal rating)
```

---

## API Response Examples

### Get User with Cart
```json
{
  "id": 1,
  "name": "John Doe",
  "email": "john@vectortech.com",
  "role": "USER",
  "cart": {
    "id": 1,
    "items": [
      {
        "id": 1,
        "product": {
          "id": 3,
          "name": "4K Webcam",
          "price": 149.99,
          "imageUrl": "https://..."
        },
        "quantity": 2
      }
    ],
    "createdAt": "2024-03-13T10:30:00"
  },
  "createdAt": "2024-03-13T09:15:00",
  "updatedAt": "2024-03-13T10:30:00"
}
```

### Get Order with Items
```json
{
  "id": 1,
  "status": "CONFIRMED",
  "totalAmount": 449.97,
  "items": [
    {
      "id": 1,
      "product": {
        "id": 1,
        "name": "Premium Wireless Headphones"
      },
      "quantity": 2,
      "price": 199.99
    },
    {
      "id": 2,
      "product": {
        "id": 3,
        "name": "4K Webcam"
      },
      "quantity": 1,
      "price": 149.99
    }
  ],
  "createdAt": "2024-03-13T10:30:00",
  "updatedAt": "2024-03-13T11:00:00"
}
```

### Get Products by Category
```json
{
  "id": 1,
  "name": "Electronics",
  "description": "Electronic gadgets and devices",
  "products": [
    {
      "id": 1,
      "name": "Premium Wireless Headphones",
      "price": 199.99,
      "stock": 50,
      "inStock": true,
      "rating": 4.80,
      "imageUrl": "https://..."
    },
    {
      "id": 3,
      "name": "4K Webcam",
      "price": 149.99,
      "stock": 40,
      "inStock": true,
      "rating": 4.70,
      "imageUrl": "https://..."
    }
  ]
}
```

---

## Best Practices

### 1. Query Performance
- Use `@EntityGraph` for eager loading related entities
- Avoid N+1 query problems
- Use indexes for frequently queried columns
- Leverage `findByCategoryId()` instead of loading all products

### 2. Data Validation
- Use Jakarta Validation annotations (`@NotNull`, `@Email`, `@Size`, etc.)
- Validate at entity level, not just at API boundary
- Constraints are enforced in database at column level

### 3. Transaction Management
- Use `@Transactional` at service layer
- Cascade operations automatically manage related entities
- Orphan removal cleans up deleted cart/order items

### 4. Password Security
- Never store plaintext passwords
- Implement BCryptPasswordEncoder in authentication
- Password validation: minimum 8 characters, max 255

### 5. Audit Trail
- All entities have `createdAt` and `updatedAt` timestamps
- Auto-populated via `@PrePersist` and `@PreUpdate`
- Useful for tracking changes and compliance

### 6. Connection Pooling
- HikariCP configuration optimized for typical e-commerce load
- Adjust `maximum-pool-size` based on actual usage metrics
- Monitor connection usage via Actuator

### 7. Cascade Operations
Review cascade strategies:
- **User → Orders:** RESTRICT (prevent user deletion with orders)
- **Cart → Items:** CASCADE (delete items when cart is deleted)
- **Order → Items:** CASCADE (delete items when order is deleted)
- **Category → Products:** RESTRICT (prevent category deletion)

---

## Testing

### Test Coverage

**Repository Tests (All Implemented):**
- ✅ UserRepositoryTest (10+ test cases)
- ✅ ProductRepositoryTest (14+ test cases)
- ✅ OrderRepositoryTest (14+ test cases)
- ✅ CartRepositoryTest (7+ test cases)
- ✅ CartItemRepositoryTest (8+ test cases)
- ✅ CategoryRepositoryTest (10+ test cases)
- ✅ OrderItemRepositoryTest (9+ test cases)

**Total: 72+ Unit Test Cases**

### Test Annotations Used
```java
@DataJpaTest              // Spring Boot test for JPA/repositories
@Autowired                // Inject repositories
@BeforeEach               // Setup test data
@Test                     // Test method
assertThat(...).isEqualTo()  // AssertJ assertions
```

### Running Tests

**Run all tests:**
```bash
mvn test
```

**Run specific test class:**
```bash
mvn test -Dtest=UserRepositoryTest
```

**Run with coverage:**
```bash
mvn test jacoco:report
```

### Key Test Scenarios Covered

1. **CRUD Operations**
   - Create, Read, Update, Delete for all entities
   - Verify persistence to database

2. **Relationships**
   - One-to-Many relationships (User→Orders)
   - One-to-One relationships (User→Cart)
   - Many-to-One relationships (Product→Category)
   - Entity graph loading with products

3. **Custom Queries**
   - Case-insensitive searches
   - Filtering by category
   - Sorting by created_at descending
   - Unique constraints (email, category name)

4. **Business Logic**
   - Stock level determines `inStock` flag
   - Cart item uniqueness constraint
   - Order status transitions
   - Historical price preservation in OrderItem

5. **Data Validation**
   - Timestamp auto-population
   - Default values (role=USER, status=PENDING)
   - Cascade operations
   - Orphan removal

---

## Database Setup Instructions

### Prerequisites

1. **PostgreSQL Installation**
   ```bash
   # Windows (choco)
   choco install postgresql
   
   # macOS (brew)
   brew install postgresql
   
   # Ubuntu/Debian
   sudo apt-get install postgresql postgresql-contrib
   ```

2. **Start PostgreSQL Service**
   ```bash
   # Windows
   net start PostgreSQL15
   
   # macOS
   brew services start postgresql
   
   # Linux
   sudo systemctl start postgresql
   ```

### Create Database

```sql
-- Connect as postgres user
psql -U postgres

-- Create database
CREATE DATABASE vectortech;

-- List databases
\l

-- Connect to database
\c vectortech

-- Verify tables created (Spring Hibernate will create them)
\dt
```

### Update application.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vectortech
    username: postgres
    password: <your_postgres_password>
```

### Run Application

```bash
cd backend
mvn spring-boot:run
```

Schema and test data created automatically on startup.

---

## Next Steps

1. ✅ **Database Design** - Complete
2. ✅ **JPA Entities** - Complete
3. ✅ **Repositories** - Complete
4. ✅ **Data Seeding** - Complete
5. ✅ **Unit Tests** - Complete
6. 🔄 **API Endpoints** - Implement CRUD services
7. 🔄 **Business Logic** - Implement cart and order services
8. 🔄 **Frontend Integration** - Connect Next.js to APIs
9. 🔄 **Security Hardening** - Add authentication/authorization
10. 🔄 **Performance Tuning** - Query optimization, caching

---

## Support & Documentation

**Spring Data JPA:** https://spring.io/projects/spring-data-jpa
**Hibernate:** https://hibernate.org/orm/
**PostgreSQL:** https://www.postgresql.org/docs/
**HikariCP:** https://github.com/brettwooldridge/HikariCP

---

**Last Updated:** March 13, 2024
**Version:** 1.0
**Status:** Production Ready
