package com.example.foodNow.controller;

import com.example.foodNow.dto.MenuItemRequest;
import com.example.foodNow.dto.MenuItemResponse;
import com.example.foodNow.service.MenuItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class MenuItemController {

    private final MenuItemService menuItemService;

    @PostMapping("/menu-items")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<MenuItemResponse> createMenuItem(@Valid @RequestBody MenuItemRequest request) {
        return new ResponseEntity<>(menuItemService.createMenuItem(request), HttpStatus.CREATED);
    }

    @PutMapping("/menu-items/{id}")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<MenuItemResponse> updateMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItemRequest request) {
        return ResponseEntity.ok(menuItemService.updateMenuItem(id, request));
    }

    @DeleteMapping("/menu-items/{id}")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        menuItemService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/restaurants/{restaurantId}/menu-items")
    public ResponseEntity<List<MenuItemResponse>> getMenuItemsByRestaurant(
            @PathVariable Long restaurantId,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        return ResponseEntity.ok(menuItemService.getMenuItemsByRestaurant(restaurantId, activeOnly));
    }
}
