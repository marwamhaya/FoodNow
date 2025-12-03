package com.example.foodNow.controller;

import com.example.foodNow.dto.PageResponse;
import com.example.foodNow.dto.RestaurantRequest;
import com.example.foodNow.dto.RestaurantResponse;
import com.example.foodNow.service.RestaurantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

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
}
