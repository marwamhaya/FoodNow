package com.example.foodNow.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MenuItemResponse {
    private Long id;
    private Long restaurantId;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
    private String category;
    private Boolean isAvailable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
