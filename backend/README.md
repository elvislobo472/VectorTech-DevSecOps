# VectorTech Backend API

Spring Boot REST API powering the VectorTech e-commerce platform.

## Tech Stack

| Layer       | Technology                          |
|-------------|-------------------------------------|
| Framework   | Spring Boot 3.2 (Java 21)           |
| Security    | Spring Security + JWT (jjwt 0.12)   |
| Database    | PostgreSQL 15+                      |
| ORM         | JPA / Hibernate                     |
| Build       | Maven                               |
| Testing     | JUnit 5 + Mockito                   |

---

## Project Structure

```
src/main/java/com/vectortech/backend/
├── controller/      ← REST controllers
├── service/         ← Business logic
├── repository/      ← Spring Data JPA interfaces
├── model/           ← JPA entities
├── dto/             ← Request / response DTOs
├── security/        ← JWT filter, UserDetailsService
├── config/          ← Security & CORS config
├── exception/       ← Custom exceptions + GlobalExceptionHandler
└── util/            ← ApiResponse wrapper
```

---

## Getting Started

### Prerequisites
- Java 21+
- Maven 3.9+
- PostgreSQL 15+

### 1. Create the database

```sql
CREATE DATABASE vectortech;
```

Then run the schema initializer:

```bash
psql -U postgres -d vectortech -f src/main/resources/schema.sql
```

### 2. Configure `application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/vectortech
    username: postgres
    password: your_password_here
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

JWT config (same file):

```yaml
app:
  jwt:
    secret: YOUR_BASE64_SECRET_HERE
    expiration-ms: 86400000
```

### 3. Run the application

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## API Reference

### Authentication

| Method | Endpoint             | Auth   | Description            |
|--------|----------------------|--------|------------------------|
| POST   | /api/auth/register   | Public | Register new user      |
| POST   | /api/auth/login      | Public | Login and get JWT      |

**Register** `POST /api/auth/register`
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

**Login** `POST /api/auth/login`
```json
{
  "email": "john@example.com",
  "password": "password123"
}
```
Response:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGci...",
    "tokenType": "Bearer",
    "userId": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "role": "USER"
  }
}
```

---

### Products

| Method | Endpoint                            | Auth       | Description               |
|--------|-------------------------------------|------------|---------------------------|
| GET    | /api/products                       | Public     | Get all products          |
| GET    | /api/products/{id}                  | Public     | Get product by ID         |
| GET    | /api/products/search?query=         | Public     | Search products           |
| GET    | /api/products/category/{category}   | Public     | Filter by category        |
| POST   | /api/products                       | ADMIN only | Create product            |
| PUT    | /api/products/{id}                  | ADMIN only | Update product            |
| DELETE | /api/products/{id}                  | ADMIN only | Delete product            |

**Create product** `POST /api/products`
```json
{
  "name": "Mechanical Keyboard",
  "description": "Hot-swappable RGB keyboard for work and gaming.",
  "price": 129.99,
  "categoryId": 3,
  "imageUrl": "https://images.example.com/keyboard.jpg",
  "stock": 40
}
```

---

### Cart  *(Authenticated users)*

| Method | Endpoint                     | Description           |
|--------|------------------------------|-----------------------|
| GET    | /api/cart                    | Get current cart      |
| POST   | /api/cart/add                | Add item to cart      |
| PUT    | /api/cart/update             | Update item quantity  |
| DELETE | /api/cart/remove/{productId} | Remove item from cart |

**Add to cart** `POST /api/cart/add`
```json
{
  "productId": 1,
  "quantity": 2
}
```

---

### Orders  *(Authenticated users)*

| Method | Endpoint             | Description          |
|--------|----------------------|----------------------|
| POST   | /api/orders/checkout | Checkout from cart   |
| GET    | /api/orders          | Get order history    |
| GET    | /api/orders/{id}     | Get order by ID      |

---

## Authentication

Include the JWT token in the `Authorization` header:

```
Authorization: Bearer <token>
```

---

## Default Admin Account

```
Email:    admin@vectortech.com
Password: Admin@123
```

> **Change this password immediately in production.**

---

## Running Tests

```bash
mvn test
```

## Security Notes

- Passwords are hashed with BCrypt.
- JWT is required for all cart and order endpoints.
- Product write operations are restricted to the `ADMIN` role.
- Unauthorized and forbidden requests return JSON error payloads.

---

## Database Schema

```
users ─────────────────────────────────────┐
  id, name, email, password, role          │
                                           │
categories ← products                      │
              │                            │
              ▼                            │
            cart_items → carts → users ───┘
                                           │
            order_items → orders → users ──┘
```

### Core Tables (PostgreSQL)

- `users`: `id`, `name`, `email` (unique), `password`, `role`, `created_at`, `updated_at`
- `categories`: `id`, `name`, `description`, `created_at`, `updated_at`
- `products`: `id`, `name`, `description`, `price`, `stock`, `image_url`, `category_id`, `created_at`, `updated_at`
- `carts`: `id`, `user_id` (unique FK), `created_at`, `updated_at`
- `cart_items`: `id`, `cart_id`, `product_id`, `quantity`
- `orders`: `id`, `user_id`, `status`, `total_amount`, `created_at`, `updated_at`
- `order_items`: `id`, `order_id`, `product_id`, `quantity`, `price`

### JPA Relationships

- `User -> Order`: one-to-many
- `User -> Cart`: one-to-one
- `Cart -> CartItem`: one-to-many
- `Product -> CartItem`: many-to-one (from cart item perspective)
- `Order -> OrderItem`: one-to-many
- `Product -> Category`: many-to-one

### Data Seeding

`src/main/java/com/vectortech/backend/config/DataInitializer.java` seeds:

- default categories
- sample products

This runs on application startup and avoids duplicate inserts by checking existing records.

### Sample API Responses (Database-backed)

`GET /api/products`

```json
{
  "success": true,
  "message": "Success",
  "data": [
    {
      "id": 1,
      "name": "Premium Wireless Headphones",
      "description": "High-quality wireless headphones with active noise cancellation and 30-hour battery life.",
      "price": 199.99,
      "category": "Electronics",
      "categoryId": 1,
      "imageUrl": "https://images.unsplash.com/...",
      "stock": 50,
      "rating": 4.80,
      "inStock": true,
      "createdAt": "2026-03-13T09:30:00"
    }
  ]
}
```

`GET /api/cart`

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "cartId": 1,
    "items": [
      {
        "id": 10,
        "productId": 1,
        "productName": "Premium Wireless Headphones",
        "imageUrl": "https://images.unsplash.com/...",
        "price": 199.99,
        "quantity": 2,
        "subtotal": 399.98
      }
    ],
    "totalAmount": 399.98,
    "totalItems": 2
  }
}
```

`GET /api/orders/1`

```json
{
  "success": true,
  "message": "Success",
  "data": {
    "id": 1,
    "userId": 2,
    "status": "PENDING",
    "totalAmount": 399.98,
    "items": [
      {
        "id": 20,
        "productId": 1,
        "productName": "Premium Wireless Headphones",
        "imageUrl": "https://images.unsplash.com/...",
        "quantity": 2,
        "price": 199.99,
        "subtotal": 399.98
      }
    ],
    "createdAt": "2026-03-13T10:05:00"
  }
}
```
