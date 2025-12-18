package com.example.foodNow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLocationDTO {
    private Long orderId;
    private Double clientLatitude;
    private Double clientLongitude;
    private Double driverLatitude;
    private Double driverLongitude;
}
