package com.example.foodNow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderActionRequest {
    private String reason; // Optional reason for rejection
}
