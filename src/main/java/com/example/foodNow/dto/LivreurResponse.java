package com.example.foodNow.dto;

import lombok.Data;

@Data
public class LivreurResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String phone;
    private String vehicleType;
    private Boolean isAvailable;
    private Boolean isActive;
    private Double latitude;
    private Double longitude;
}
