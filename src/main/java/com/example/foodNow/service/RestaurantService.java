package com.example.foodNow.service;

import com.example.foodNow.dto.OrderItemResponse;
import com.example.foodNow.dto.OrderResponse;
import com.example.foodNow.dto.PageResponse;
import com.example.foodNow.dto.RestaurantRequest;
import com.example.foodNow.dto.RestaurantResponse;
import com.example.foodNow.exception.ResourceNotFoundException;
import com.example.foodNow.model.Order;
import com.example.foodNow.model.OrderItem;
import com.example.foodNow.model.Restaurant;
import com.example.foodNow.model.User;
import com.example.foodNow.repository.OrderRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RestaurantService {

        private final RestaurantRepository restaurantRepository;
        private final UserRepository userRepository;
        private final OrderRepository orderRepository;
        private final PasswordEncoder passwordEncoder;

        @Transactional
        public RestaurantResponse createRestaurant(RestaurantRequest request) {
                // Admin provides owner details in the request. Create a new User with
                // RESTAURANT role.
                if (userRepository.existsByEmail(request.getOwnerEmail())) {
                        throw new IllegalArgumentException("A user with the provided email already exists");
                }

                User owner = new User();
                owner.setEmail(request.getOwnerEmail());
                owner.setPassword(passwordEncoder.encode(request.getOwnerPassword()));
                owner.setFullName(request.getOwnerFullName());
                owner.setPhoneNumber(request.getOwnerPhoneNumber());
                owner.setRole(User.Role.RESTAURANT);
                owner.setIsActive(true);

                User savedOwner = userRepository.save(owner);

                Restaurant restaurant = new Restaurant();
                restaurant.setName(request.getName());
                restaurant.setAddress(request.getAddress());
                restaurant.setDescription(request.getDescription());
                restaurant.setPhone(request.getPhone());
                restaurant.setImageUrl(request.getImageUrl());
                restaurant.setOwner(savedOwner);
                restaurant.setIsActive(true); // Active by default or false pending validation? Prompt says
                                              // "créer/valider",
                                              // let's assume active for now or admin decides.

                Restaurant savedRestaurant = restaurantRepository.save(restaurant);
                return mapToResponse(savedRestaurant);
        }

        @Transactional
        public RestaurantResponse updateRestaurant(Long id, RestaurantRequest request) {
                Restaurant restaurant = restaurantRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Restaurant not found with id: " + id));

                validateOwnershipOrAdmin(restaurant);

                restaurant.setName(request.getName());
                restaurant.setAddress(request.getAddress());
                restaurant.setDescription(request.getDescription());
                restaurant.setPhone(request.getPhone());
                restaurant.setImageUrl(request.getImageUrl());

                // Explicitly update the updated_at field
                restaurant.setUpdatedAt(java.time.LocalDateTime.now());

                // Update Owner details
                User owner = restaurant.getOwner();
                boolean ownerUpdated = false;

                if (request.getOwnerEmail() != null && !request.getOwnerEmail().isEmpty()
                                && !request.getOwnerEmail().equals(owner.getEmail())) {
                        if (userRepository.existsByEmail(request.getOwnerEmail())) {
                                throw new IllegalArgumentException("A user with the provided email already exists");
                        }
                        owner.setEmail(request.getOwnerEmail());
                        ownerUpdated = true;
                }

                if (request.getOwnerPassword() != null && !request.getOwnerPassword().isEmpty()) {
                        owner.setPassword(passwordEncoder.encode(request.getOwnerPassword()));
                        ownerUpdated = true;
                }

                if (ownerUpdated) {
                        userRepository.save(owner);
                }

                Restaurant updatedRestaurant = restaurantRepository.save(restaurant);
                return mapToResponse(updatedRestaurant);
        }

        @Transactional
        public void toggleRestaurantStatus(Long id) {
                Restaurant restaurant = restaurantRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Restaurant not found with id: " + id));

                restaurant.setIsActive(!restaurant.getIsActive());
                restaurantRepository.save(restaurant);
        }

        public RestaurantResponse getRestaurantById(Long id) {
                Restaurant restaurant = restaurantRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Restaurant not found with id: " + id));
                return mapToResponse(restaurant);
        }

        public PageResponse<RestaurantResponse> getAllRestaurants(int pageNo, int pageSize, String sortBy,
                        String sortDir) {
                Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();

                Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
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
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No restaurant found for current user"));
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
                String principal = authentication.getName();

                try {
                        // Try to parse as Long (ID)
                        Long userId = Long.parseLong(principal);
                        return userRepository.findById(userId)
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "User not found with id: " + userId));
                } catch (NumberFormatException e) {
                        // If not a number, try to find by email
                        return userRepository.findByEmail(principal)
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "User not found with email: " + principal));
                }
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

        /**
         * Get all orders for the current restaurant owner with pagination
         */
        public PageResponse<OrderResponse> getMyRestaurantOrders(int pageNo, int pageSize, String sortBy,
                        String sortDir) {
                User currentUser = getCurrentUser();
                Restaurant restaurant = restaurantRepository.findByOwnerId(currentUser.getId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No restaurant found for current user"));

                return getRestaurantOrders(restaurant.getId(), pageNo, pageSize, sortBy, sortDir);
        }

        /**
         * Get orders for a specific restaurant by ID (admin or owner only)
         */
        public PageResponse<OrderResponse> getRestaurantOrders(Long restaurantId, int pageNo, int pageSize,
                        String sortBy, String sortDir) {
                Restaurant restaurant = restaurantRepository.findById(restaurantId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Restaurant not found with id: " + restaurantId));

                validateOwnershipOrAdmin(restaurant);

                Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();

                Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
                Page<Order> orders = orderRepository.findByRestaurantId(restaurantId, pageable);

                List<OrderResponse> content = orders.getContent().stream()
                                .map(this::mapToOrderResponse)
                                .collect(Collectors.toList());

                return new PageResponse<>(content, orders.getNumber(), orders.getSize(),
                                orders.getTotalElements(), orders.getTotalPages(), orders.isLast());
        }

        /**
         * Get orders for the current restaurant filtered by status
         */
        public PageResponse<OrderResponse> getMyRestaurantOrdersByStatus(Order.OrderStatus status,
                        int pageNo, int pageSize,
                        String sortBy, String sortDir) {
                User currentUser = getCurrentUser();
                Restaurant restaurant = restaurantRepository.findByOwnerId(currentUser.getId())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No restaurant found for current user"));

                Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending()
                                : Sort.by(sortBy).descending();

                Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
                Page<Order> orders = orderRepository.findByRestaurantIdAndStatus(restaurant.getId(), status, pageable);

                List<OrderResponse> content = orders.getContent().stream()
                                .map(this::mapToOrderResponse)
                                .collect(Collectors.toList());

                return new PageResponse<>(content, orders.getNumber(), orders.getSize(),
                                orders.getTotalElements(), orders.getTotalPages(), orders.isLast());
        }

        /**
         * Get a specific order by ID (owner or admin only)
         */
        public OrderResponse getOrderById(Long orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Order not found with id: " + orderId));

                // Verify that the current user owns the restaurant for this order
                validateOwnershipOrAdmin(order.getRestaurant());

                return mapToOrderResponse(order);
        }

        /**
         * Get all order items for a specific order (owner or admin only)
         * This allows restaurants to view just the items without the full order details
         */
        public List<OrderItemResponse> getOrderItems(Long orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Order not found with id: " + orderId));

                // Verify that the current user owns the restaurant for this order
                validateOwnershipOrAdmin(order.getRestaurant());

                // Return only the order items
                return order.getOrderItems().stream()
                                .map(this::mapToOrderItemResponse)
                                .collect(Collectors.toList());
        }

        /**
         * Accept an order (change status from PENDING to ACCEPTED)
         * Only the restaurant owner or admin can accept orders
         */
        @Transactional
        public OrderResponse acceptOrder(Long orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Order not found with id: " + orderId));

                // Verify ownership
                validateOwnershipOrAdmin(order.getRestaurant());

                // Validate current status
                if (order.getStatus() != Order.OrderStatus.PENDING) {
                        throw new IllegalStateException(
                                        "Only orders with PENDING status can be accepted. Current status: "
                                                        + order.getStatus());
                }

                // Update status to ACCEPTED
                order.setStatus(Order.OrderStatus.ACCEPTED);
                Order updatedOrder = orderRepository.save(order);

                return mapToOrderResponse(updatedOrder);
        }

        /**
         * Reject/Cancel an order (change status to CANCELLED)
         * Only the restaurant owner or admin can reject orders
         */
        @Transactional
        public OrderResponse rejectOrder(Long orderId, String reason) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Order not found with id: " + orderId));

                // Verify ownership
                validateOwnershipOrAdmin(order.getRestaurant());

                // Validate current status - can only reject PENDING or ACCEPTED orders
                if (order.getStatus() != Order.OrderStatus.PENDING &&
                                order.getStatus() != Order.OrderStatus.ACCEPTED) {
                        throw new IllegalStateException(
                                        "Only PENDING or ACCEPTED orders can be rejected. Current status: "
                                                        + order.getStatus());
                }

                // Update status to CANCELLED
                order.setStatus(Order.OrderStatus.CANCELLED);
                // Note: If you want to store the rejection reason, you'll need to add a field
                // to the Order entity
                Order updatedOrder = orderRepository.save(order);

                return mapToOrderResponse(updatedOrder);
        }

        /**
         * Update order status to PREPARING
         * This is typically done after accepting the order
         */
        @Transactional
        public OrderResponse startPreparingOrder(Long orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Order not found with id: " + orderId));

                validateOwnershipOrAdmin(order.getRestaurant());

                if (order.getStatus() != Order.OrderStatus.ACCEPTED) {
                        throw new IllegalStateException(
                                        "Only ACCEPTED orders can be moved to PREPARING. Current status: "
                                                        + order.getStatus());
                }

                order.setStatus(Order.OrderStatus.PREPARING);
                Order updatedOrder = orderRepository.save(order);

                return mapToOrderResponse(updatedOrder);
        }

        /**
         * Update order status to READY_FOR_PICKUP
         */
        @Transactional
        public OrderResponse markOrderReady(Long orderId) {
                Order order = orderRepository.findById(orderId)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Order not found with id: " + orderId));

                validateOwnershipOrAdmin(order.getRestaurant());

                if (order.getStatus() != Order.OrderStatus.PREPARING) {
                        throw new IllegalStateException(
                                        "Only PREPARING orders can be marked as READY. Current status: "
                                                        + order.getStatus());
                }

                order.setStatus(Order.OrderStatus.READY_FOR_PICKUP);
                Order updatedOrder = orderRepository.save(order);

                return mapToOrderResponse(updatedOrder);
        }

        /**
         * Map Order entity to OrderResponse DTO
         */
        private OrderResponse mapToOrderResponse(Order order) {
                OrderResponse response = new OrderResponse();
                response.setId(order.getId());
                response.setClientId(order.getClient().getId());
                response.setClientName(order.getClient().getFullName());
                response.setClientPhone(order.getClient().getPhoneNumber());
                response.setRestaurantId(order.getRestaurant().getId());
                response.setRestaurantName(order.getRestaurant().getName());
                response.setTotalAmount(order.getTotalAmount());
                response.setStatus(order.getStatus());
                response.setDeliveryAddress(order.getDeliveryAddress());
                response.setCreatedAt(order.getCreatedAt());
                response.setUpdatedAt(order.getUpdatedAt());

                // Map order items
                List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
                                .map(this::mapToOrderItemResponse)
                                .collect(Collectors.toList());
                response.setOrderItems(orderItemResponses);

                return response;
        }

        /**
         * Map OrderItem entity to OrderItemResponse DTO
         */
        private OrderItemResponse mapToOrderItemResponse(OrderItem orderItem) {
                OrderItemResponse response = new OrderItemResponse();
                response.setId(orderItem.getId());
                response.setMenuItemId(orderItem.getMenuItem().getId());
                response.setMenuItemName(orderItem.getMenuItem().getName());
                response.setQuantity(orderItem.getQuantity());
                response.setUnitPrice(orderItem.getUnitPrice());
                response.setSubtotal(orderItem.getSubtotal());
                return response;
        }
}
