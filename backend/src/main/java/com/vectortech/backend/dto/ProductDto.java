package com.vectortech.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;
    private Long categoryId;
    private String imageUrl;
    private Integer stock;
    private BigDecimal rating;
    private boolean inStock;
    private LocalDateTime createdAt;
}
