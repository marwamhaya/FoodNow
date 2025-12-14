package com.example.foodNow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelectedOptionResponse {
    private String name;
    private BigDecimal price;
}
