package com.example.foodNow.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DeliveryResponse {
    private Long id;
    private Long orderId;
    private String restaurantName;
    private String restaurantAddress;
    private String clientName;
    private String clientAddress;
    private String clientPhone;
    private String status;
    private LocalDateTime pickupTime;
    private LocalDateTime deliveryTime;
    private LocalDateTime createdAt;
    private Integer rating;
    private String ratingComment;
}
