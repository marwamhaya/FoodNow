package com.example.foodNow.controller;

import com.example.foodNow.dto.RatingRequest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ratings")
@RequiredArgsConstructor
public class RatingController {

    private final com.example.foodNow.service.RestaurantRatingService restaurantRatingService;

    @PostMapping("/restaurant") // Explicit endpoint for restaurant rating
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> submitRestaurantRating(@RequestBody RatingRequest request) {
        restaurantRatingService.submitRating(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/restaurant/my-ratings")
    @PreAuthorize("hasRole('RESTAURANT')")
    public ResponseEntity<java.util.List<com.example.foodNow.dto.RestaurantRatingResponse>> getMyRatings() {
        return ResponseEntity.ok(restaurantRatingService.getRatingsForCurrentRestaurant());
    }

    @GetMapping("/restaurant/{id}")
    public ResponseEntity<java.util.List<com.example.foodNow.dto.RestaurantRatingResponse>> getRestaurantRatings(
            @PathVariable Long id) {
        return ResponseEntity.ok(restaurantRatingService.getRatingsByRestaurantId(id));
    }

    // Keeping existing separate if needed, or deprecating.
    // Ideally we would merge logic or keep separate if rating "Delivery" vs
    // "Restaurant".
    // User task says "Rate Restaurant".
    // I will add the new endpoint.
}
