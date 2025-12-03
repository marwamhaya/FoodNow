package com.example.foodNow.service;

import com.example.foodNow.dto.PageResponse;
import com.example.foodNow.dto.RestaurantRequest;
import com.example.foodNow.dto.RestaurantResponse;
import com.example.foodNow.exception.ResourceNotFoundException;
import com.example.foodNow.model.Restaurant;
import com.example.foodNow.model.User;
import com.example.foodNow.repository.RestaurantRepository;
import com.example.foodNow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Transactional
    public RestaurantResponse createRestaurant(RestaurantRequest request) {
        if (restaurantRepository.existsByName(request.getName())) {
            throw new IllegalArgumentException("Restaurant with name " + request.getName() + " already exists");
        }

        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getOwnerId()));

        if (owner.getRole() != User.Role.RESTAURANT) {
            throw new IllegalArgumentException("User must have RESTAURANT role to own a restaurant");
        }

        // Check if user already owns a restaurant (optional rule, assuming 1 restaurant
        // per user for now based on prompt context "son propre profil")
        if (restaurantRepository.findByOwnerId(owner.getId()).isPresent()) {
            throw new IllegalArgumentException("User already owns a restaurant");
        }

        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setDescription(request.getDescription());
        restaurant.setPhone(request.getPhone());
        restaurant.setImageUrl(request.getImageUrl());
        restaurant.setOwner(owner);
        restaurant.setIsActive(true); // Active by default or false pending validation? Prompt says "créer/valider",
                                      // let's assume active for now or admin decides.

        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return mapToResponse(savedRestaurant);
    }

    @Transactional
    public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));

        validateOwnershipOrAdmin(restaurant);

        restaurant.setName(request.getName());
        restaurant.setAddress(request.getAddress());
        restaurant.setDescription(request.getDescription());
        restaurant.setPhone(request.getPhone());
        restaurant.setImageUrl(request.getImageUrl());
        // Owner cannot be changed via update usually, so ignoring ownerId in request

        Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
        return mapToResponse(updatedRestaurant);
    }

    @Transactional
    public void toggleRestaurantStatus(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));

        restaurant.setIsActive(!restaurant.getIsActive());
        restaurantRepository.save(restaurant);
    }

    public RestaurantResponse getRestaurantById(Long id) {
        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Restaurant not found with id: " + id));
        return mapToResponse(restaurant);
    }

    public PageResponse<RestaurantResponse> getAllRestaurants(int pageNo, int pageSize, String sortBy, String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        // Admin sees all, others see only active?
        // Prompt says "listing paginé/filtré". Usually public listing is active only.
        // Let's assume this is the public listing.
        Page<Restaurant> restaurants = restaurantRepository.findAllByIsActiveTrue(pageable);

        List<RestaurantResponse> content = restaurants.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(content, restaurants.getNumber(), restaurants.getSize(),
                restaurants.getTotalElements(), restaurants.getTotalPages(), restaurants.isLast());
    }

    public PageResponse<RestaurantResponse> getAllRestaurantsAdmin(int pageNo, int pageSize, String sortBy,
            String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Page<Restaurant> restaurants = restaurantRepository.findAll(pageable);

        List<RestaurantResponse> content = restaurants.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new PageResponse<>(content, restaurants.getNumber(), restaurants.getSize(),
                restaurants.getTotalElements(), restaurants.getTotalPages(), restaurants.isLast());
    }

    public RestaurantResponse getMyRestaurant() {
        User currentUser = getCurrentUser();
        Restaurant restaurant = restaurantRepository.findByOwnerId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No restaurant found for current user"));
        return mapToResponse(restaurant);
    }

    private void validateOwnershipOrAdmin(Restaurant restaurant) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == User.Role.ADMIN) {
            return;
        }
        if (!restaurant.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to update this restaurant");
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private RestaurantResponse mapToResponse(Restaurant restaurant) {
        RestaurantResponse response = new RestaurantResponse();
        response.setId(restaurant.getId());
        response.setName(restaurant.getName());
        response.setAddress(restaurant.getAddress());
        response.setDescription(restaurant.getDescription());
        response.setPhone(restaurant.getPhone());
        response.setImageUrl(restaurant.getImageUrl());
        response.setIsActive(restaurant.getIsActive());
        response.setOwnerId(restaurant.getOwner().getId());
        response.setOwnerName(restaurant.getOwner().getFullName());
        response.setCreatedAt(restaurant.getCreatedAt());
        response.setUpdatedAt(restaurant.getUpdatedAt());
        return response;
    }
}
