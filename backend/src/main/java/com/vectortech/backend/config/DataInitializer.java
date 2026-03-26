package com.vectortech.backend.config;

import java.math.BigDecimal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.vectortech.backend.model.Category;
import com.vectortech.backend.model.Product;
import com.vectortech.backend.repository.CategoryRepository;
import com.vectortech.backend.repository.ProductRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void run(String... args) {
        seedCategoriesAndProducts();
    }

    private void seedCategoriesAndProducts() {
        Category electronics = upsertCategory("Electronics", "Electronic gadgets and devices");
        Category wearables = upsertCategory("Wearables", "Smartwatches, fitness trackers and wearable tech");
        Category accessories = upsertCategory("Accessories", "Computer and device accessories");
        Category storage = upsertCategory("Storage", "Portable and internal storage solutions");

        upsertProduct(
                "Premium Wireless Headphones",
                "High-quality wireless headphones with active noise cancellation and 30-hour battery life.",
                new BigDecimal("199.99"),
                50,
                "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&h=500&fit=crop",
                electronics,
                new BigDecimal("4.80")
        );

        upsertProduct(
                "Smartwatch Pro",
                "Advanced smartwatch with fitness tracking, heart rate monitor, and sleep analysis.",
                new BigDecimal("299.99"),
                30,
                "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=500&h=500&fit=crop",
                wearables,
                new BigDecimal("4.60")
        );

        upsertProduct(
                "4K Webcam",
                "Professional 4K webcam with auto-focus and built-in microphone for streaming and video calls.",
                new BigDecimal("149.99"),
                40,
                "https://images.unsplash.com/photo-1598327105666-5b89351aff97?w=500&h=500&fit=crop",
                electronics,
                new BigDecimal("4.70")
        );

        upsertProduct(
                "Mechanical Keyboard",
                "RGB mechanical keyboard with custom switches and programmable keys for gaming and work.",
                new BigDecimal("129.99"),
                60,
                "https://images.unsplash.com/photo-1587829191301-11f190cce7f1?w=500&h=500&fit=crop",
                accessories,
                new BigDecimal("4.90")
        );

        upsertProduct(
                "Portable SSD 1TB",
                "Fast portable SSD with 1TB storage capacity, perfect for backup and file transfer.",
                new BigDecimal("89.99"),
                75,
                "https://images.unsplash.com/photo-1597872200969-2b65d56bd16b?w=500&h=500&fit=crop",
                storage,
                new BigDecimal("4.50")
        );

        upsertProduct(
                "USB-C Hub",
                "7-in-1 USB-C hub with multiple ports for charging and data transfer.",
                new BigDecimal("49.99"),
                0,
                "https://images.unsplash.com/photo-1625948515291-69613efd103f?w=500&h=500&fit=crop",
                accessories,
                new BigDecimal("4.40")
        );

        log.info("Data seeding complete: categories={}, products={}",
                categoryRepository.count(), productRepository.count());
    }

    private Category upsertCategory(String name, String description) {
        return categoryRepository.findByName(name)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .name(name)
                        .description(description)
                        .build()));
    }

    private void upsertProduct(
            String name,
            String description,
            BigDecimal price,
            Integer stock,
            String imageUrl,
            Category category,
            BigDecimal rating
    ) {
                if (productRepository.existsByNameIgnoreCase(name)) {
                        return;
                }

                productRepository.save(Product.builder()
                                .name(name)
                                .description(description)
                                .price(price)
                                .stock(stock)
                                .imageUrl(imageUrl)
                                .category(category)
                                .rating(rating)
                                .build());
    }
}
