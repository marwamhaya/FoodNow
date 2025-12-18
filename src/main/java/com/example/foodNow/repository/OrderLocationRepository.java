package com.example.foodNow.repository;

import com.example.foodNow.model.OrderLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderLocationRepository extends JpaRepository<OrderLocation, Long> {
    Optional<OrderLocation> findByOrderId(Long orderId);

    void deleteByOrderId(Long orderId);
}
