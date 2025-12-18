 package com.example.foodNow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(nullable = false, name = "client_latitude")
    private Double clientLatitude;

    @Column(nullable = false, name = "client_longitude")
    private Double clientLongitude;

    @Column(name = "driver_latitude")
    private Double driverLatitude;

    @Column(name = "driver_longitude")
    private Double driverLongitude;

    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
