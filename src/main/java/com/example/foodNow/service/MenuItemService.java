package com.example.foodNow.service;

import com.example.foodNow.dto.MenuItemRequest;
import com.example.foodNow.dto.MenuItemResponse;
import com.example.foodNow.exception.ResourceNotFoundException;
import com.example.foodNow.model.MenuItem;
import com.example.foodNow.model.Restaurant;
import com.example.foodNow.model.User;
import com.example.foodNow.repository.MenuItemRepository;
import com.example.foodNow.repository.RestaurantRepository;
import com.example.foodNow.dto.MenuOptionGroupResponse;
import com.example.foodNow.dto.MenuOptionResponse;
import com.example.foodNow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Transactional
    public MenuItemResponse createMenuItem(MenuItemRequest request) {
        Restaurant restaurant = getMyRestaurant();

        MenuItem menuItem = new MenuItem();
        menuItem.setRestaurant(restaurant);
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setCategory(request.getCategory());
        menuItem.setIsAvailable(request.getIsAvailable() != null ? request.getIsAvailable() : true);

        if (request.getOptionGroups() != null) {
            List<com.example.foodNow.model.MenuOptionGroup> groups = request.getOptionGroups().stream()
                    .map(groupRequest -> {
                        com.example.foodNow.model.MenuOptionGroup group = new com.example.foodNow.model.MenuOptionGroup();
                        group.setName(groupRequest.getName());
                        group.setRequired(groupRequest.isRequired());
                        group.setMultiple(groupRequest.isMultiple());
                        group.setMenuItem(menuItem);

                        if (groupRequest.getOptions() != null) {
                            List<com.example.foodNow.model.MenuOption> options = groupRequest.getOptions().stream()
                                    .map(optionRequest -> {
                                        com.example.foodNow.model.MenuOption option = new com.example.foodNow.model.MenuOption();
                                        option.setName(optionRequest.getName());
                                        option.setExtraPrice(optionRequest.getExtraPrice());
                                        option.setOptionGroup(group);
                                        return option;
                                    }).collect(Collectors.toList());
                            group.setOptions(options);
                        }
                        return group;
                    }).collect(Collectors.toList());
            menuItem.setOptionGroups(groups);
        }

        MenuItem savedMenuItem = menuItemRepository.save(menuItem);
        return mapToResponse(savedMenuItem);
    }

    @Transactional
    public MenuItemResponse updateMenuItem(Long id, MenuItemRequest request) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));

        validateOwnership(menuItem.getRestaurant());

        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        menuItem.setPrice(request.getPrice());
        menuItem.setImageUrl(request.getImageUrl());
        menuItem.setCategory(request.getCategory());
        if (request.getIsAvailable() != null) {
            menuItem.setIsAvailable(request.getIsAvailable());
        }
        menuItem.setUpdatedAt(java.time.LocalDateTime.now());

        MenuItem updatedMenuItem = menuItemRepository.save(menuItem);
        return mapToResponse(updatedMenuItem);
    }

    @Transactional
    public void deleteMenuItem(Long id) {
        MenuItem menuItem = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found with id: " + id));

        validateOwnership(menuItem.getRestaurant());

        menuItemRepository.delete(menuItem);
    }

    public List<MenuItemResponse> getMenuItemsByRestaurant(Long restaurantId, boolean activeOnly) {
        List<MenuItem> items;
        if (activeOnly) {
            items = menuItemRepository.findByRestaurantIdAndIsAvailableTrue(restaurantId);
        } else {
            items = menuItemRepository.findByRestaurantId(restaurantId);
        }
        return items.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private Restaurant getMyRestaurant() {
        User currentUser = getCurrentUser();
        return restaurantRepository.findByOwnerId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No restaurant found for current user"));
    }

    private void validateOwnership(Restaurant restaurant) {
        User currentUser = getCurrentUser();
        if (currentUser.getRole() == User.Role.ADMIN) {
            return;
        }
        if (!restaurant.getOwner().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to manage this restaurant's menu");
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principal = authentication.getName();

        try {
            // Try to parse as Long (ID)
            Long userId = Long.parseLong(principal);
            return userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        } catch (NumberFormatException e) {
            // If not a number, try to find by email
            return userRepository.findByEmail(principal)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + principal));
        }
    }

    private MenuItemResponse mapToResponse(MenuItem menuItem) {
        MenuItemResponse response = new MenuItemResponse();
        response.setId(menuItem.getId());
        response.setRestaurantId(menuItem.getRestaurant().getId());
        response.setName(menuItem.getName());
        response.setDescription(menuItem.getDescription());
        response.setPrice(menuItem.getPrice());
        response.setImageUrl(menuItem.getImageUrl());
        response.setCategory(menuItem.getCategory());
        response.setIsAvailable(menuItem.getIsAvailable());
        response.setCreatedAt(menuItem.getCreatedAt());
        response.setUpdatedAt(menuItem.getUpdatedAt());

        if (menuItem.getOptionGroups() != null) {
            List<MenuOptionGroupResponse> groups = menuItem.getOptionGroups().stream()
                    .map(group -> {
                        MenuOptionGroupResponse groupResponse = new MenuOptionGroupResponse();
                        groupResponse.setId(group.getId());
                        groupResponse.setName(group.getName());
                        groupResponse.setRequired(group.isRequired()); // Lombok likely generates isRequired() for
                                                                       // boolean
                        groupResponse.setMultiple(group.isMultiple());

                        if (group.getOptions() != null) {
                            List<MenuOptionResponse> options = group.getOptions().stream()
                                    .map(option -> {
                                        MenuOptionResponse optionResponse = new MenuOptionResponse();
                                        optionResponse.setId(option.getId());
                                        optionResponse.setName(option.getName());
                                        optionResponse.setExtraPrice(option.getExtraPrice());
                                        return optionResponse;
                                    }).collect(Collectors.toList());
                            groupResponse.setOptions(options);
                        }
                        return groupResponse;
                    }).collect(Collectors.toList());
            response.setOptionGroups(groups);
        }

        return response;
    }
}
