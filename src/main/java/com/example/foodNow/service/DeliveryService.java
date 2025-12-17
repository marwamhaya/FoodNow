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
    private final com.example.foodNow.repository.LivreurRepository livreurRepository;
    private final OrderLocationService orderLocationService;

    @Transactional
    public DeliveryResponse assignDelivery(Long orderId, Long driverId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found"));

        if (deliveryRepository.findByDriverIdAndStatus(driverId, Delivery.DeliveryStatus.DELIVERY_ACCEPTED)
                .size() > 0) {
            // Optional: Check if driver is already busy?
        }

        Delivery delivery = new Delivery();
        delivery.setOrder(order);
        delivery.setDriver(driver);
        delivery.setStatus(Delivery.DeliveryStatus.DELIVERY_ACCEPTED);

        Delivery savedDelivery = deliveryRepository.save(delivery);

        // Update Order status?
        order.setStatus(Order.OrderStatus.IN_DELIVERY); // Or READY_FOR_PICKUP?
        orderRepository.save(order);

        return mapToResponse(savedDelivery);
    }

    @Transactional
    public void createDeliveryRequest(Order order) {
        // idempotent check
        if (deliveryRepository.findByOrderId(order.getId()).isPresent()) {
            return;
        }

        Delivery delivery = new Delivery();
        delivery.setOrder(order);
        // No driver assigned yet
        delivery.setStatus(Delivery.DeliveryStatus.PENDING);
        deliveryRepository.save(delivery);
    }

    public List<DeliveryResponse> getAvailableDeliveryRequests() {
        return deliveryRepository.findByStatus(Delivery.DeliveryStatus.PENDING).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Assigning a driver now means accepting a PENDING request
    @Transactional
    public DeliveryResponse acceptDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery request not found"));

        if (delivery.getStatus() != Delivery.DeliveryStatus.PENDING) {
            throw new IllegalStateException("Delivery is not available");
        }

        User currentUser = getCurrentUser();
        // Optional: Check if driver already has an active delivery

        delivery.setDriver(currentUser);
        delivery.setStatus(Delivery.DeliveryStatus.DELIVERY_ACCEPTED);

        // Update Order Status
        delivery.getOrder().setStatus(Order.OrderStatus.IN_DELIVERY); // Or keep PREPARING until pickup?
        // Let's keep existing flow: Order goes to PREPARING -> READY -> IN_DELIVERY.
        // If driver accepts, it just means they are assigned.
        // Order status shouldn't change to IN_DELIVERY until pickup.
        // But for now, let's leave Order status as is (PREPARING or READY).

        orderRepository.save(delivery.getOrder());
        return mapToResponse(deliveryRepository.save(delivery));
    }

    public List<DeliveryResponse> getAssignedDeliveries() {
        User currentUser = getCurrentUser();
        List<Delivery> deliveries = deliveryRepository.findByDriverIdAndStatus(currentUser.getId(),
                Delivery.DeliveryStatus.DELIVERY_ACCEPTED);
        deliveries.addAll(
                deliveryRepository.findByDriverIdAndStatus(currentUser.getId(), Delivery.DeliveryStatus.PICKED_UP));
        deliveries.addAll(
                deliveryRepository.findByDriverIdAndStatus(currentUser.getId(), Delivery.DeliveryStatus.ON_THE_WAY));
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

            // Update Livreur stats
            com.example.foodNow.model.Livreur livreur = livreurRepository.findByUserId(delivery.getDriver().getId())
                    .orElse(null);
            if (livreur != null) {
                livreur.setCompletedDeliveries(livreur.getCompletedDeliveries() + 1);
                livreurRepository.save(livreur);
            }

            // Clean up GPS location data
            orderLocationService.deleteOrderLocation(delivery.getOrder().getId());
        }

        orderRepository.save(delivery.getOrder());
        Delivery savedDelivery = deliveryRepository.save(delivery);
        return mapToResponse(savedDelivery);
    }

    @Transactional
    public void rateDelivery(Long deliveryId, int rating, String comment) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));

        delivery.setRating(rating);
        delivery.setRatingComment(comment);
        deliveryRepository.save(delivery);

        // Update average rating for Livreur
        com.example.foodNow.model.Livreur livreur = livreurRepository.findByUserId(delivery.getDriver().getId())
                .orElse(null);
        if (livreur != null) {
            livreur.setRatingSum(livreur.getRatingSum() + rating);
            livreur.setRatingCount(livreur.getRatingCount() + 1);
            livreurRepository.save(livreur);
        }
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
