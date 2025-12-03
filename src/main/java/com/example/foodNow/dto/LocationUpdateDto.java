package com.example.foodNow.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationUpdateDto {
    @NotNull(message = "Latitude is required")
    private Double latitude;

    @NotNull(message = "Longitude is required")
    private Double longitude;
}
