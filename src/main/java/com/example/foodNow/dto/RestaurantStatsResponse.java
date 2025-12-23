package com.example.foodNow.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RestaurantStatsResponse {
    private Long totalOrders;
    private BigDecimal totalRevenue;
    private Double averageRating;
    private Integer ratingCount;
    private Integer totalClients; // Number of unique clients

    public RestaurantStatsResponse(Long totalOrders, BigDecimal totalRevenue, Double averageRating, Integer ratingCount,
            Integer totalClients) {
        this.totalOrders = totalOrders;
        this.totalRevenue = totalRevenue;
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
        this.totalClients = totalClients;
    }
}
