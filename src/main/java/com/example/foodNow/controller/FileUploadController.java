package com.example.foodNow.controller;

import com.example.foodNow.service.FileStorageService;
import com.example.foodNow.service.RestaurantService;
import com.example.foodNow.service.MenuItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileStorageService fileStorageService;
    private final RestaurantService restaurantService;
    private final MenuItemService menuItemService;

    @PostMapping("/restaurant/{restaurantId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESTAURANT')")
    public ResponseEntity<Map<String, String>> uploadRestaurantImage(
            @PathVariable Long restaurantId,
            @RequestParam("file") MultipartFile file) {

        String imageUrl = fileStorageService.storeRestaurantImage(restaurantId, file);
        restaurantService.updateRestaurantImage(restaurantId, imageUrl);

        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);
        response.put("message", "Image uploaded successfully");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/menu-item/{menuItemId}")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<Map<String, String>> uploadMenuItemImage(
            @PathVariable Long menuItemId,
            @RequestParam("file") MultipartFile file) {

        String imageUrl = fileStorageService.storeMenuItemImage(menuItemId, file);
        menuItemService.updateMenuItemImage(menuItemId, imageUrl);

        Map<String, String> response = new HashMap<>();
        response.put("imageUrl", imageUrl);
        response.put("message", "Image uploaded successfully");

        return ResponseEntity.ok(response);
    }
}
