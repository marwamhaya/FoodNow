package com.example.foodNow.dto;

import lombok.Data;

@Data
public class RatingRequest {
    private Long orderId;
    private Integer rating;
    private String comment;
}
