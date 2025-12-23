package com.example.foodNow.service;

import com.example.foodNow.dto.RatingRequest;
import com.example.foodNow.exception.ResourceNotFoundException;
import com.example.foodNow.model.Order;
import com.example.foodNow.model.Restaurant;
import com.example.foodNow.model.RestaurantRating;
import com.example.foodNow.model.User;
import com.example.foodNow.repository.OrderRepository;
import com.example.foodNow.repository.RestaurantRatingRepository;
import com.example.foodNow.repository.RestaurantRepository;
import com.example.foodNow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantRatingService {

    private final RestaurantRatingRepository ratingRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Transactional
    public void submitRating(RatingRequest request) {
        User currentUser = getCurrentUser();

        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!order.getClient().getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You can only rate your own orders");
        }

        if (order.getStatus() != Order.OrderStatus.DELIVERED) {
            throw new IllegalArgumentException("Order must be delivered to leave a rating");
        }

        Restaurant restaurant = order.getRestaurant();

        // Check if rating exists
        RestaurantRating rating = ratingRepository.findByOrderId(order.getId())
                .orElse(new RestaurantRating());

        rating.setRestaurant(restaurant);
        rating.setClient(currentUser);
        rating.setOrderId(order.getId());
        rating.setRatingValue(request.getRating());
        rating.setComment(request.getComment());

        ratingRepository.save(rating);

        updateRestaurantStats(restaurant);
    }

    private void updateRestaurantStats(Restaurant restaurant) {
        List<RestaurantRating> ratings = ratingRepository.findByRestaurantId(restaurant.getId());
        double avg = ratings.stream().mapToInt(RestaurantRating::getRatingValue).average().orElse(0.0);
        restaurant.setAverageRating(Math.round(avg * 10.0) / 10.0); // Round to 1 decimal
        restaurant.setRatingCount(ratings.size());
        restaurantRepository.save(restaurant);
    }

    public List<com.example.foodNow.dto.RestaurantRatingResponse> getRatingsForCurrentRestaurant() {
        User currentUser = getCurrentUser();
        Restaurant restaurant = restaurantRepository.findByOwnerId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found for this user"));

        return ratingRepository.findByRestaurantId(restaurant.getId()).stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(rating -> com.example.foodNow.dto.RestaurantRatingResponse.builder()
                        .id(rating.getId())
                        .clientName(rating.getClient().getFullName())
                        .rating(rating.getRatingValue())
                        .comment(rating.getComment())
                        .createdAt(rating.getCreatedAt())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    public List<com.example.foodNow.dto.RestaurantRatingResponse> getRatingsByRestaurantId(Long restaurantId) {
        return ratingRepository.findByRestaurantId(restaurantId).stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(rating -> com.example.foodNow.dto.RestaurantRatingResponse.builder()
                        .id(rating.getId())
                        .clientName(rating.getClient().getFullName())
                        .rating(rating.getRatingValue())
                        .comment(rating.getComment())
                        .createdAt(rating.getCreatedAt())
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    private User getCurrentUser() {
        String principal = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            return userRepository.findById(Long.parseLong(principal)).orElseThrow();
        } catch (NumberFormatException e) {
            return userRepository.findByEmail(principal).orElseThrow();
        }
    }
}
