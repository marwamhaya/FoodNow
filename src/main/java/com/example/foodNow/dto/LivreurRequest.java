package com.example.foodNow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LivreurRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Vehicle type is required")
    private String vehicleType; // VELO, SCOOTER, MOTO, VOITURE
}
