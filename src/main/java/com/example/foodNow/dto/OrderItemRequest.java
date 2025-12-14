package com.example.foodNow.dto;

import lombok.Data;

@Data
public class OrderItemRequest {
    private Long menuItemId;
    private Integer quantity;
    private java.util.List<Long> selectedOptionIds;
}
