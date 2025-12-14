package com.example.foodNow.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class MenuOptionResponse {
    private Long id;
    private String name;
    private BigDecimal extraPrice;
}
