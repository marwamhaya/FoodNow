package com.example.foodNow.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RestaurantRatingResponse {
    private Long id;
    private String clientName;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;
}
