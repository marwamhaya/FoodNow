package com.example.foodNow.repository;

import com.example.foodNow.model.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
    Page<Restaurant> findAllByIsActiveTrue(Pageable pageable);

    Optional<Restaurant> findByOwnerId(Long ownerId);

    boolean existsByName(String name);
}
