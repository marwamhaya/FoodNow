package com.example.foodNow.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private String status; // "SUCCESS", "FAILED"
    private String message;
}
