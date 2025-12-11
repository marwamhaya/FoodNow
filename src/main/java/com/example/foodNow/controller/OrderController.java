package com.example.foodNow.controller;

import com.example.foodNow.model.Order;
import com.example.foodNow.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/restaurant")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<List<com.example.foodNow.dto.OrderResponse>> getRestaurantOrders() {
        return ResponseEntity.ok(orderService.getOrdersByRestaurant());
    }

    @GetMapping("/client")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<List<com.example.foodNow.dto.OrderResponse>> getClientOrders() {
        return ResponseEntity.ok(orderService.getOrdersByClient());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<com.example.foodNow.dto.OrderResponse>> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT')")
    public ResponseEntity<com.example.foodNow.dto.OrderResponse> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }
}
