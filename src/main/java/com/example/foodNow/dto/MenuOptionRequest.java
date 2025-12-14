package com.example.foodNow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MenuOptionRequest {

    @NotBlank(message = "Option name is required")
    private String name;

    @NotNull(message = "Extra price is required")
    @PositiveOrZero(message = "Extra price must be zero or positive")
    private BigDecimal extraPrice;
}
