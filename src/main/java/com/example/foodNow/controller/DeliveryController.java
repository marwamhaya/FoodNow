package com.example.foodNow.controller;

import com.example.foodNow.dto.DeliveryResponse;
import com.example.foodNow.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT')")
    public ResponseEntity<DeliveryResponse> assignDelivery(
            @RequestParam Long orderId,
            @RequestParam Long driverId) {
        return ResponseEntity.ok(deliveryService.assignDelivery(orderId, driverId));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('LIVREUR')")
    public ResponseEntity<DeliveryResponse> updateDeliveryStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(deliveryService.updateDeliveryStatus(id, status));
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasRole('LIVREUR')")
    public ResponseEntity<List<DeliveryResponse>> getAssignedDeliveries() {
        return ResponseEntity.ok(deliveryService.getAssignedDeliveries());
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('LIVREUR')")
    public ResponseEntity<List<DeliveryResponse>> getDeliveryHistory() {
        return ResponseEntity.ok(deliveryService.getDeliveryHistory());
    }
}
