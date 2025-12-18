package com.example.foodNow.service;

import com.example.foodNow.dto.LocationDTO;
import com.example.foodNow.dto.OrderLocationDTO;
import com.example.foodNow.model.Order;
import com.example.foodNow.model.OrderLocation;
import com.example.foodNow.repository.OrderLocationRepository;
import com.example.foodNow.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderLocationService {

    private final OrderLocationRepository orderLocationRepository;
    private final OrderRepository orderRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public void saveClientLocation(Long orderId, LocationDTO locationDTO) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        OrderLocation orderLocation = orderLocationRepository.findByOrderId(orderId)
                .orElse(new OrderLocation());

        orderLocation.setOrder(order);
        orderLocation.setClientLatitude(locationDTO.getLatitude());
        orderLocation.setClientLongitude(locationDTO.getLongitude());

        orderLocationRepository.save(orderLocation);

        // Broadcast client location to driver if delivery is assigned
        messagingTemplate.convertAndSend(
                "/topic/delivery/" + orderId + "/client-location",
                locationDTO);
    }

    @Transactional
    public void updateDriverLocation(Long orderId, LocationDTO locationDTO) {
        OrderLocation orderLocation = orderLocationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order location not found: " + orderId));

        orderLocation.setDriverLatitude(locationDTO.getLatitude());
        orderLocation.setDriverLongitude(locationDTO.getLongitude());

        orderLocationRepository.save(orderLocation);

        // Broadcast driver location to client
        messagingTemplate.convertAndSend(
                "/topic/delivery/" + orderId + "/location",
                locationDTO);
    }

    public OrderLocationDTO getOrderLocation(Long orderId) {
        OrderLocation orderLocation = orderLocationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Order location not found: " + orderId));

        return new OrderLocationDTO(
                orderId,
                orderLocation.getClientLatitude(),
                orderLocation.getClientLongitude(),
                orderLocation.getDriverLatitude(),
                orderLocation.getDriverLongitude());
    }

    @Transactional
    public void deleteOrderLocation(Long orderId) {
        orderLocationRepository.deleteByOrderId(orderId);
    }
}
