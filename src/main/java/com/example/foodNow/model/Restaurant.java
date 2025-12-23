package com.example.foodNow.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "business_name", nullable = false, length = 100)
    private String businessName;

    @Column(nullable = false, length = 255)
    private String address;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "opening_hours")
    private String openingHours;

    @Column(name = "average_rating")
    private Double averageRating = 0.0;

    @Column(name = "rating_count")
    private Integer ratingCount = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    public void syncFields() {
        this.updatedAt = LocalDateTime.now();
        this.businessName = this.name;
        if (this.owner != null) {
            this.ownerId = this.owner.getId();
        }
    }
}
