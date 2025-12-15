package com.example.foodNow.service;

import com.example.foodNow.exception.ResourceNotFoundException;
import com.example.foodNow.model.Restaurant;
import com.example.foodNow.model.User;
import com.example.foodNow.dto.RestaurantResponse;
import com.example.foodNow.repository.OrderRepository;
import com.example.foodNow.repository.RestaurantRepository;
import com.example.foodNow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", orderRepository.count());
        stats.put("totalRevenue", orderRepository.sumTotalAmount());
        stats.put("totalUsers", userRepository.count());
        stats.put("totalRestaurants", restaurantRepository.count());

        // New Users (last 30 days)
        stats.put("newUsersCount", userRepository.countByCreatedAtAfter(LocalDateTime.now().minusDays(30)));

        // Delivery Performance (Completed Orders rate)
        long delivered = orderRepository.countByStatus(com.example.foodNow.model.Order.OrderStatus.DELIVERED);
        long totalOrders = orderRepository.count();
        double performance = totalOrders > 0 ? ((double) delivered / totalOrders) * 100 : 0.0;
        stats.put("deliveryPerformance", Math.round(performance * 100.0) / 100.0);

        return stats;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
    }

    public List<RestaurantResponse> getAllRestaurants() {
        return restaurantRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private RestaurantResponse mapToResponse(Restaurant restaurant) {
        RestaurantResponse response = new RestaurantResponse();
        response.setId(restaurant.getId());
        response.setName(restaurant.getName());
        response.setAddress(restaurant.getAddress());
        response.setDescription(restaurant.getDescription());
        response.setPhone(restaurant.getPhone());
        response.setImageUrl(restaurant.getImageUrl());
        response.setIsActive(restaurant.getIsActive());
        if (restaurant.getOwner() != null) {
            response.setOwnerId(restaurant.getOwner().getId());
            response.setOwnerName(restaurant.getOwner().getFullName());
        }
        response.setCreatedAt(restaurant.getCreatedAt());
        response.setUpdatedAt(restaurant.getUpdatedAt());
        return response;
    }

    @Transactional
    public void toggleRestaurantStatus(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        restaurant.setIsActive(!restaurant.getIsActive());
        restaurantRepository.save(restaurant);
    }

    @Transactional
    public void resetUserPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public long getDailyOrderCount(Long restaurantId) {
        LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = java.time.LocalDate.now().atTime(java.time.LocalTime.MAX);
        return orderRepository.countByRestaurantIdAndCreatedAtBetween(restaurantId, startOfDay, endOfDay);
    }
}
