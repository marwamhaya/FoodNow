package com.example.foodNow.repository;

import com.example.foodNow.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find all orders for a specific restaurant with pagination
    Page<Order> findByRestaurantId(Long restaurantId, Pageable pageable);

    // Find orders by restaurant and status
    Page<Order> findByRestaurantIdAndStatus(Long restaurantId, Order.OrderStatus status, Pageable pageable);

    // Find all orders for a specific restaurant (without pagination)
    List<Order> findByRestaurantId(Long restaurantId);

    // Find orders by client
    Page<Order> findByClientId(Long clientId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT SUM(o.totalAmount) FROM Order o")
    java.math.BigDecimal sumTotalAmount();

    long countByStatus(com.example.foodNow.model.Order.OrderStatus status);

    long countByRestaurantIdAndCreatedAtBetween(Long restaurantId, LocalDateTime start, LocalDateTime end);
}
