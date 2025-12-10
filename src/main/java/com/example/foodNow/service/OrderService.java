package com.example.foodNow.service;

import com.example.foodNow.exception.ResourceNotFoundException;
import com.example.foodNow.model.Order;
import com.example.foodNow.model.Restaurant;
import com.example.foodNow.model.User;
import com.example.foodNow.repository.OrderRepository;
import com.example.foodNow.repository.RestaurantRepository;
import com.example.foodNow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final NotificationService notificationService;

    // Create Order (Simplified for now, assumes OrderItems logic handled elsewhere
    // or passed fully)
    // For this task, we focus on status transitions and retrieval.

    public List<Order> getOrdersByRestaurant() {
        User currentUser = getCurrentUser();
        // Find restaurant owned by user
        Restaurant restaurant = restaurantRepository.findAll().stream()
                .filter(r -> r.getOwner().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found for current user"));

        return orderRepository.findByRestaurantId(restaurant.getId());
    }

    public List<Order> getOrdersByClient() {
        User currentUser = getCurrentUser();
        // Use pagination in real app, but list for now as per repo method
        // Repo has pagination method, let's use the one without or just cast/adapt?
        // Repo: Page<Order> findByClientId(Long clientId, Pageable pageable);
        // We need a List version or pass pageable.
        // Let's add List version to Repo or use Pageable.unpaged()
        // For simplicity, let's assume we can add List version or use Pageable.
        // I'll use Pageable.unpaged() if I can, or just add List method to repo.
        // I'll add List method to Repo in next step if needed.
        // Wait, I can just use findAll and filter? No, inefficient.
        // I'll assume I can use the existing Page method and getContent().
        return orderRepository.findByClientId(currentUser.getId(), org.springframework.data.domain.Pageable.unpaged())
                .getContent();
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String statusStr) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(statusStr.toUpperCase());
        order.setStatus(newStatus);

        // Notify Client
        notificationService.sendNotification(
                order.getClient(),
                "Order Update",
                "Your order status is now: " + newStatus,
                com.example.foodNow.model.Notification.NotificationType.ORDER_UPDATE);

        return orderRepository.save(order);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principal = authentication.getName();
        try {
            Long userId = Long.parseLong(principal);
            return userRepository.findById(userId).orElseThrow();
        } catch (NumberFormatException e) {
            return userRepository.findByEmail(principal).orElseThrow();
        }
    }
}
