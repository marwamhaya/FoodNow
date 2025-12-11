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

    public java.util.List<com.example.foodNow.dto.OrderResponse> getOrdersByRestaurant() {
        User currentUser = getCurrentUser();
        // Find restaurant owned by user
        Restaurant restaurant = restaurantRepository.findAll().stream()
                .filter(r -> r.getOwner().getId().equals(currentUser.getId()))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found for current user"));

        return orderRepository.findByRestaurantId(restaurant.getId()).stream()
                .map(this::mapToOrderResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    public java.util.List<com.example.foodNow.dto.OrderResponse> getOrdersByClient() {
        User currentUser = getCurrentUser();
        return orderRepository.findByClientId(currentUser.getId(), org.springframework.data.domain.Pageable.unpaged())
                .getContent().stream()
                .map(this::mapToOrderResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    public java.util.List<com.example.foodNow.dto.OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToOrderResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public com.example.foodNow.dto.OrderResponse updateOrderStatus(Long orderId, String statusStr) {
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

        return mapToOrderResponse(orderRepository.save(order));
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

    private com.example.foodNow.dto.OrderResponse mapToOrderResponse(Order order) {
        com.example.foodNow.dto.OrderResponse response = new com.example.foodNow.dto.OrderResponse();
        response.setId(order.getId());
        response.setClientId(order.getClient().getId());
        response.setClientName(order.getClient().getFullName());
        response.setClientPhone(order.getClient().getPhoneNumber());
        response.setRestaurantId(order.getRestaurant().getId());
        response.setRestaurantName(order.getRestaurant().getName());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        // Map order items
        java.util.List<com.example.foodNow.dto.OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
                .map(this::mapToOrderItemResponse)
                .collect(java.util.stream.Collectors.toList());
        response.setOrderItems(orderItemResponses);

        return response;
    }

    private com.example.foodNow.dto.OrderItemResponse mapToOrderItemResponse(
            com.example.foodNow.model.OrderItem orderItem) {
        com.example.foodNow.dto.OrderItemResponse response = new com.example.foodNow.dto.OrderItemResponse();
        response.setId(orderItem.getId());
        response.setMenuItemId(orderItem.getMenuItem().getId());
        response.setMenuItemName(orderItem.getMenuItem().getName());
        response.setQuantity(orderItem.getQuantity());
        response.setUnitPrice(orderItem.getUnitPrice());
        response.setSubtotal(orderItem.getSubtotal());
        return response;
    }
}
