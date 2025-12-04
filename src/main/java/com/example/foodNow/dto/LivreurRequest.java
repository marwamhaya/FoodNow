package com.example.foodNow.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LivreurRequest {

    // Admin creates the livreur user with these fields
    @NotBlank(message = "User email is required")
    @Email(message = "Invalid email format")
    private String userEmail;

    @NotBlank(message = "User password is required")
    private String userPassword;

    @NotBlank(message = "User full name is required")
    private String userFullName;

    private String userPhoneNumber;

    @NotNull(message = "Vehicle type is required")
    private String vehicleType; // VELO, SCOOTER, MOTO, VOITURE
}
