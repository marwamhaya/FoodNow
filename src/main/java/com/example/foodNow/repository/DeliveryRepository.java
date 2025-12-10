package com.example.foodNow.repository;

import com.example.foodNow.model.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    List<Delivery> findByDriverIdAndStatus(Long driverId, Delivery.DeliveryStatus status);

    List<Delivery> findByDriverId(Long driverId);
}
