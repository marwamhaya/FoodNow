package com.example.foodNow.service;

import com.example.foodNow.dto.DeliveryResponse;
import com.example.foodNow.exception.ResourceNotFoundException;
import com.example.foodNow.model.Delivery;
import com.example.foodNow.model.Order;
import com.example.foodNow.model.User;
import com.example.foodNow.repository.DeliveryRepository;
import com.example.foodNow.repository.OrderRepository;
import com.example.foodNow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeliveryService {

    private final DeliveryRepository deliveryRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public DeliveryResponse assignDelivery(Long orderId, Long driverId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        if (deliveryRepository.findByDriverIdAndStatus(driverId, Delivery.DeliveryStatus.ASSIGNED).size() > 0) {
            // Optional: Check if driver is already busy?
        }

        Delivery delivery = new Delivery();
        delivery.setOrder(order);
        delivery.setDriver(driver);
        delivery.setStatus(Delivery.DeliveryStatus.ASSIGNED);

        Delivery savedDelivery = deliveryRepository.save(delivery);

        // Update Order status?
        order.setStatus(Order.OrderStatus.IN_DELIVERY); // Or READY_FOR_PICKUP?
        orderRepository.save(order);

        return mapToResponse(savedDelivery);
    }

    public List<DeliveryResponse> getAssignedDeliveries() {
        User currentUser = getCurrentUser();
        List<Delivery> deliveries = deliveryRepository.findByDriverIdAndStatus(currentUser.getId(),
                Delivery.DeliveryStatus.ASSIGNED);
        deliveries.addAll(
                deliveryRepository.findByDriverIdAndStatus(currentUser.getId(), Delivery.DeliveryStatus.PICKED_UP));
        return deliveries.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<DeliveryResponse> getDeliveryHistory() {
        User currentUser = getCurrentUser();
        return deliveryRepository.findByDriverId(currentUser.getId()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DeliveryResponse updateDeliveryStatus(Long deliveryId, String statusStr) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + deliveryId));

        User currentUser = getCurrentUser();
        if (!delivery.getDriver().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to update this delivery");
        }

        Delivery.DeliveryStatus newStatus;
        try {
            newStatus = Delivery.DeliveryStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + statusStr);
        }

        delivery.setStatus(newStatus);

        if (newStatus == Delivery.DeliveryStatus.PICKED_UP) {
            delivery.setPickupTime(LocalDateTime.now());
            delivery.getOrder().setStatus(Order.OrderStatus.IN_DELIVERY);
        } else if (newStatus == Delivery.DeliveryStatus.DELIVERED) {
            delivery.setDeliveryTime(LocalDateTime.now());
            delivery.getOrder().setStatus(Order.OrderStatus.DELIVERED);
        }

        orderRepository.save(delivery.getOrder());
        Delivery savedDelivery = deliveryRepository.save(delivery);
        return mapToResponse(savedDelivery);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principal = authentication.getName();

        try {
            Long userId = Long.parseLong(principal);
            return userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        } catch (NumberFormatException e) {
            return userRepository.findByEmail(principal)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + principal));
        }
    }

    private DeliveryResponse mapToResponse(Delivery delivery) {
        DeliveryResponse response = new DeliveryResponse();
        response.setId(delivery.getId());
        response.setOrderId(delivery.getOrder().getId());
        response.setRestaurantName(delivery.getOrder().getRestaurant().getName());
        response.setRestaurantAddress(delivery.getOrder().getRestaurant().getAddress());
        response.setClientName(delivery.getOrder().getClient().getFullName());
        response.setClientAddress(delivery.getOrder().getDeliveryAddress());
        response.setClientPhone(delivery.getOrder().getClient().getPhoneNumber());
        response.setStatus(delivery.getStatus().name());
        response.setPickupTime(delivery.getPickupTime());
        response.setDeliveryTime(delivery.getDeliveryTime());
        response.setCreatedAt(delivery.getCreatedAt());
        return response;
    }
}
