package com.example.foodNow.service;

import com.example.foodNow.exception.ResourceNotFoundException;
import com.example.foodNow.model.Restaurant;
import com.example.foodNow.model.User;
import com.example.foodNow.repository.OrderRepository;
import com.example.foodNow.repository.RestaurantRepository;
import com.example.foodNow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final OrderRepository orderRepository;

    public Map<String, Object> getSystemStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalOrders", orderRepository.count());
        stats.put("totalRevenue", orderRepository.sumTotalAmount());
        stats.put("totalUsers", userRepository.count());
        // Add more stats as needed
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

    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    @Transactional
    public void toggleRestaurantStatus(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found"));
        restaurant.setIsActive(!restaurant.getIsActive());
        restaurantRepository.save(restaurant);
    }
}
