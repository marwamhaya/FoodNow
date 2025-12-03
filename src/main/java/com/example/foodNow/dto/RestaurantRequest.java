package com.example.foodNow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RestaurantRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    private String description;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Invalid phone number format")
    private String phone;

    private String imageUrl;

    @NotNull(message = "Owner ID is required")
    private Long ownerId;
}
