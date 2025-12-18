package com.example.foodNow.controller;

import com.example.foodNow.dto.RatingRequest;
import com.example.foodNow.model.Delivery;
import com.example.foodNow.repository.DeliveryRepository;
import com.example.foodNow.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final DeliveryService deliveryService;
    private final DeliveryRepository deliveryRepository;

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> submitRating(@RequestBody RatingRequest request) {
        // Find delivery by orderId
        Delivery delivery = deliveryRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new RuntimeException("Delivery not found for order: " + request.getOrderId()));

        if (delivery.getStatus() != Delivery.DeliveryStatus.DELIVERED) {
            return ResponseEntity.badRequest().build();
        }

        deliveryService.rateDelivery(delivery.getId(), request.getRating(), request.getComment());
        return ResponseEntity.ok().build();
    }
}
