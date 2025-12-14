package com.example.foodNow.repository;

import com.example.foodNow.model.MenuOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuOptionRepository extends JpaRepository<MenuOption, Long> {
}
