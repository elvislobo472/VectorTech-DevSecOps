package com.vectortech.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products",
        indexes = {
            @Index(name = "idx_product_category", columnList = "category_id"),
            @Index(name = "idx_product_name",     columnList = "name")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(nullable = false, length = 255)
    private String name;

    @Size(max = 2000)
    @Column(columnDefinition = "TEXT")
    private String description;

    @NotNull
    @DecimalMin("0.0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Size(max = 500)
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Min(0)
    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0;

    @DecimalMin("0.0")
    @DecimalMax("5.0")
    @Column(precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "in_stock", nullable = false)
    @Builder.Default
    private boolean inStock = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        inStock = stock != null && stock > 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        inStock = stock != null && stock > 0;
    }
}
