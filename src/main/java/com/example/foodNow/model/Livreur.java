package com.example.foodNow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "livreurs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Livreur {

    public enum VehicleType {
        VELO, SCOOTER, MOTO, VOITURE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    @Column(name = "is_available", nullable = false)
    private Boolean isAvailable = false;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "current_latitude")
    private Double currentLatitude;

    @Column(name = "current_longitude")
    private Double currentLongitude;
}
