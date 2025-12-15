package com.example.foodNow.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RestaurantResponse {
    private Long id;
    private String name;
    private String address;
    private String description;
    private String phone;
    private String imageUrl;
    private String openingHours;
    private Boolean isActive;
    private Long ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
