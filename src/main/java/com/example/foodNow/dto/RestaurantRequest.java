package com.example.foodNow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class RestaurantRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Address is required")
    private String address;

    private String description;

    @NotBlank(message = "Phone number is required")

    private String phone;

    private String imageUrl;

    private String openingHours;

    // Owner fields - admin will create the restaurant and the associated restaurant
    // user

    @Email(message = "Invalid email format")
    private String ownerEmail;

    private String ownerPassword;

    private String ownerFullName;

    private String ownerPhoneNumber;
}
