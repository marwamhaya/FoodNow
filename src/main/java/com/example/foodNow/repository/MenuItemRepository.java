package com.example.foodNow.repository;

import com.example.foodNow.model.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {
    List<MenuItem> findByRestaurantId(Long restaurantId);

    List<MenuItem> findByRestaurantIdAndIsAvailableTrue(Long restaurantId);

    @org.springframework.data.jpa.repository.Query("SELECT oi.menuItem FROM OrderItem oi GROUP BY oi.menuItem ORDER BY SUM(oi.quantity) DESC")
    org.springframework.data.domain.Page<MenuItem> findMostPopularItems(
            org.springframework.data.domain.Pageable pageable);
}
