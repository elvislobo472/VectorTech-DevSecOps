-- Insert categories
INSERT INTO categories (id, name, description, created_at, updated_at) VALUES
(1, 'Electronics', 'Electronic devices and gadgets', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Laptops', 'Computers and portable devices', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Accessories', 'Tech accessories and peripherals', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'Smartphones', 'Mobile phones and tablets', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Smartwear', 'Wearable technology', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Align category sequence with seeded IDs
SELECT setval('categories_id_seq', (SELECT COALESCE(MAX(id), 1) FROM categories));

-- Align product sequence with seeded IDs
SELECT setval('products_id_seq', (SELECT COALESCE(MAX(id), 1) FROM products));

-- Insert products with prices in Indian Rupees
INSERT INTO products (id, name, description, price, stock, image_url, category_id, rating, in_stock, created_at, updated_at) VALUES
(1, 'Gaming Laptop Pro', 'High-performance laptop for gaming and professional work with RTX 4060 graphics card', 89999.00, 15, 'https://images.unsplash.com/photo-1588405748066-461ecb040aa9?w=500&h=500&fit=crop', 2, 4.50, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Wireless Headphones', 'Premium noise-cancelling wireless headphones with 30-hour battery', 4999.00, 25, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&h=500&fit=crop', 3, 4.20, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'Smart Watch Pro', 'Advanced fitness tracking smartwatch with AMOLED display', 14999.00, 30, 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&h=500&fit=crop', 5, 4.40, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, '4K Webcam', 'Professional 4K Ultra HD webcam for streaming and video calls', 8999.00, 18, 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=500&h=500&fit=crop', 3, 4.10, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(5, 'Mechanical Keyboard', 'RGB mechanical keyboard with hot-swappable switches', 6999.00, 22, 'https://images.unsplash.com/photo-1587829191301-4a118ecb1d4f?w=500&h=500&fit=crop', 3, 4.30, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(6, 'Ultra Thin Laptop', 'Lightweight ultrabook with 16GB RAM and 512GB SSD', 74999.00, 10, 'https://images.unsplash.com/photo-1516321318423-f06f70259b6c?w=500&h=500&fit=crop', 2, 4.60, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(7, 'Wireless Mouse Pro', 'Precision ergonomic wireless mouse with fast charging', 1999.00, 40, 'https://images.unsplash.com/photo-1527814050087-3793815479db?w=500&h=500&fit=crop', 3, 4.00, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(8, 'USB-C Hub', '7-in-1 USB-C multiport adapter hub with HDMI and charging', 2499.00, 35, 'https://images.unsplash.com/photo-1625948515291-69613efd103f?w=500&h=500&fit=crop', 3, 4.25, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(9, 'Portable SSD 1TB', 'Fast external solid state drive with 1050MB/s speed', 9999.00, 20, 'https://images.unsplash.com/photo-1597872514101-4af03e98dd00?w=500&h=500&fit=crop', 1, 4.55, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(10, 'Phone Stand', 'Adjustable phone stand for desk and streaming', 799.00, 50, 'https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=500&h=500&fit=crop', 3, 4.10, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(11, 'Tablet 11-inch', 'Premium tablet with 120Hz display and stylus support', 34999.00, 12, 'https://images.unsplash.com/photo-1526573612764-f7b82a3f629c?w=500&h=500&fit=crop', 4, 4.35, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(12, 'Power Bank 30000mAh', 'High-capacity fast charging power bank with dual ports', 3499.00, 45, 'https://images.unsplash.com/photo-1595348910684-f7b5b00b9ef6?w=500&h=500&fit=crop', 3, 4.20, true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

-- Align user sequence with seeded IDs
SELECT setval('users_id_seq', (SELECT COALESCE(MAX(id), 1) FROM users));

-- Insert test users
INSERT INTO users (id, name, email, password, role, created_at, updated_at) VALUES
(1, 'Admin User', 'admin@vectortech.com', '$2a$10$slYQmyNdGzin7olVN3p5Be7DlH.PKZbv5H8KnzzVgXXbVxzy1LjCq', 'ADMIN', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(2, 'Test User', 'test@example.com', '$2a$10$slYQmyNdGzin7olVN3p5Be7DlH.PKZbv5H8KnzzVgXXbVxzy1LjCq', 'USER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;
