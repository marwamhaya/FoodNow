package com.example.foodNow.controller;

import com.example.foodNow.model.Restaurant;
import com.example.foodNow.model.User;
import com.example.foodNow.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PatchMapping("/users/{id}/status")
    public ResponseEntity<Void> toggleUserStatus(@PathVariable Long id) {
        adminService.toggleUserStatus(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/restaurants")
    public ResponseEntity<List<Restaurant>> getAllRestaurants() {
        return ResponseEntity.ok(adminService.getAllRestaurants());
    }

    @PatchMapping("/restaurants/{id}/status")
    public ResponseEntity<Void> toggleRestaurantStatus(@PathVariable Long id) {
        adminService.toggleRestaurantStatus(id);
        return ResponseEntity.ok().build();
    }
}
