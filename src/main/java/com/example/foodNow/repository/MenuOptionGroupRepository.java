package com.example.foodNow.repository;

import com.example.foodNow.model.MenuOptionGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuOptionGroupRepository extends JpaRepository<MenuOptionGroup, Long> {
}
