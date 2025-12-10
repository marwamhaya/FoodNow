package com.example.foodNow.repository;

import com.example.foodNow.model.DriverLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverLocationRepository extends JpaRepository<DriverLocation, Long> {
    List<DriverLocation> findByDriverId(Long driverId);
}
