package com.example.foodNow.controller;

import com.example.foodNow.dto.LocationDTO;
import com.example.foodNow.dto.OrderLocationDTO;
import com.example.foodNow.model.Order;
import com.example.foodNow.service.OrderLocationService;
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
    private final OrderLocationService orderLocationService;

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

    @PutMapping("/{id}/accept")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<com.example.foodNow.dto.OrderResponse> acceptOrder(@PathVariable Long id) {
        // In a real app, verify restaurant ownership here
        return ResponseEntity.ok(orderService.updateOrderStatus(id, "PREPARING"));
    }

    @PutMapping("/{id}/decline")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<com.example.foodNow.dto.OrderResponse> declineOrder(@PathVariable Long id) {
        // In a real app, verify restaurant ownership here
        return ResponseEntity.ok(orderService.updateOrderStatus(id, "DECLINED"));
    }

    @PostMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<com.example.foodNow.dto.OrderResponse> createOrder(
            @RequestBody com.example.foodNow.dto.OrderRequest request) {
        return ResponseEntity.ok(orderService.createOrder(request));
    }

    // Location endpoints for GPS tracking
    @PostMapping("/{orderId}/location")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> saveOrderLocation(
            @PathVariable Long orderId,
            @RequestBody LocationDTO locationDTO) {
        orderLocationService.saveClientLocation(orderId, locationDTO);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{orderId}/location")
    @PreAuthorize("hasAnyRole('CLIENT', 'LIVREUR')")
    public ResponseEntity<OrderLocationDTO> getOrderLocation(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderLocationService.getOrderLocation(orderId));
    }
}
