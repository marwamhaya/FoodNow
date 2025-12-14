package com.example.foodNow.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentRequest {
    private BigDecimal amount;
    private String paymentMethod; // "CARD_SIMULATION", "CASH_ON_DELIVERY"
}
