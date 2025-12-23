package com.example.foodNow.controller;

import com.example.foodNow.dto.OrderActionRequest;
import com.example.foodNow.dto.OrderItemResponse;
import com.example.foodNow.dto.MenuItemResponse;
import com.example.foodNow.dto.OrderResponse;
import com.example.foodNow.dto.PageResponse;
import com.example.foodNow.dto.RestaurantRequest;
import com.example.foodNow.dto.RestaurantResponse;
import com.example.foodNow.model.Order;
import com.example.foodNow.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final com.example.foodNow.service.MenuItemService menuItemService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RestaurantResponse> createRestaurant(@Valid @RequestBody RestaurantRequest request) {
        return new ResponseEntity<>(restaurantService.createRestaurant(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT')")
    public ResponseEntity<RestaurantResponse> updateRestaurant(
            @PathVariable Long id,
            @Valid @RequestBody RestaurantRequest request) {
        return ResponseEntity.ok(restaurantService.updateRestaurant(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleStatus(@PathVariable Long id) {
        restaurantService.toggleRestaurantStatus(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RestaurantResponse> getRestaurantById(@PathVariable Long id) {
        return ResponseEntity.ok(restaurantService.getRestaurantById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<RestaurantResponse>> getAllRestaurants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(restaurantService.getAllRestaurants(page, size, sortBy, sortDir));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<RestaurantResponse>> getAllRestaurantsAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        return ResponseEntity.ok(restaurantService.getAllRestaurantsAdmin(page, size, sortBy, sortDir));
    }

    @GetMapping("/my-restaurant")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<RestaurantResponse> getMyRestaurant() {
        return ResponseEntity.ok(restaurantService.getMyRestaurant());
    }

    @GetMapping("/my-stats")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<com.example.foodNow.dto.RestaurantStatsResponse> getMyStats(
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(restaurantService.getRestaurantStats(token));
    }

    /**
     * Get all orders for the current restaurant owner
     */
    @GetMapping("/my-restaurant/orders")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<PageResponse<OrderResponse>> getMyRestaurantOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(restaurantService.getMyRestaurantOrders(page, size, sortBy, sortDir));
    }

    /**
     * Get orders for the current restaurant filtered by status
     */
    @GetMapping("/my-restaurant/orders/status/{status}")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<PageResponse<OrderResponse>> getMyRestaurantOrdersByStatus(
            @PathVariable Order.OrderStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(restaurantService.getMyRestaurantOrdersByStatus(status, page, size, sortBy, sortDir));
    }

    /**
     * Get a specific order by ID (for restaurant owner or admin)
     */
    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'ADMIN')")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(restaurantService.getOrderById(orderId));
    }

    /**
     * Get only the items of a specific order (for restaurant owner or admin)
     * Useful for displaying detailed item information separately
     */
    @GetMapping("/orders/{orderId}/items")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'ADMIN')")
    public ResponseEntity<List<OrderItemResponse>> getOrderItems(@PathVariable Long orderId) {
        return ResponseEntity.ok(restaurantService.getOrderItems(orderId));
    }

    /**
     * Get all orders for a specific restaurant (admin only)
     */
    @GetMapping("/{restaurantId}/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<OrderResponse>> getRestaurantOrders(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        return ResponseEntity.ok(restaurantService.getRestaurantOrders(restaurantId, page, size, sortBy, sortDir));
    }

    // ==================== MENUS ====================

    @GetMapping("/{id}/menu")
    public ResponseEntity<List<MenuItemResponse>> getRestaurantMenu(@PathVariable Long id) {
        return ResponseEntity.ok(menuItemService.getMenuItemsByRestaurant(id, true));
    }

    // ==================== ORDER MANAGEMENT ====================

    /**
     * Accept an order (PENDING → ACCEPTED)
     */
    @PatchMapping("/orders/{orderId}/accept")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'ADMIN')")
    public ResponseEntity<OrderResponse> acceptOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(restaurantService.acceptOrder(orderId));
    }

    /**
     * Reject/Cancel an order (PENDING/ACCEPTED → CANCELLED)
     */
    @PatchMapping("/orders/{orderId}/reject")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'ADMIN')")
    public ResponseEntity<OrderResponse> rejectOrder(
            @PathVariable Long orderId,
            @RequestBody(required = false) OrderActionRequest request) {
        String reason = (request != null && request.getReason() != null)
                ? request.getReason()
                : "No reason provided";
        return ResponseEntity.ok(restaurantService.rejectOrder(orderId, reason));
    }

    /**
     * Start preparing an order (ACCEPTED → PREPARING)
     */
    @PatchMapping("/orders/{orderId}/prepare")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'ADMIN')")
    public ResponseEntity<OrderResponse> startPreparingOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(restaurantService.startPreparingOrder(orderId));
    }

    /**
     * Mark order as ready for pickup (PREPARING → READY_FOR_PICKUP)
     */
    @PatchMapping("/orders/{orderId}/ready")
    @PreAuthorize("hasAnyRole('RESTAURANT', 'ADMIN')")
    public ResponseEntity<OrderResponse> markOrderReady(@PathVariable Long orderId) {
        return ResponseEntity.ok(restaurantService.markOrderReady(orderId));
    }
}