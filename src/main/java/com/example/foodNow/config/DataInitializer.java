package com.example.foodNow.config;

import com.example.foodNow.model.User;
import com.example.foodNow.repository.UserRepository;
import com.example.foodNow.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final com.example.foodNow.repository.RestaurantRepository restaurantRepository;
        private final com.example.foodNow.repository.MenuItemRepository menuItemRepository;
        private final OrderRepository orderRepository;

        @Bean
        public CommandLineRunner initData() {
                return args -> {
                        User admin = userRepository.findByEmail("admin@test.com").orElse(null);

                        if (admin == null) {
                                log.info("Creating default ADMIN user...");
                                admin = new User();
                                admin.setEmail("admin@test.com");
                                admin.setPassword(passwordEncoder.encode("admin123"));
                                admin.setFullName("Super Admin");
                                admin.setPhoneNumber("0000000000");
                                admin.setRole(User.Role.ADMIN);
                                admin.setIsActive(true);
                                admin = userRepository.save(admin);

                                log.info("Default ADMIN user created!");
                        }

                        // Also create a specific Restaurant Owner if needed, or just use Admin as owner
                        // for simplicity.
                        // Let's create a Restaurant Owner separate from Admin to be safe, or use Admin.
                        // Based on previous DataLoader, we used admin@test.com as owner.
                        // Reuse admin for restaurants.

                        if (restaurantRepository.count() == 0) {
                                log.info("Seeding restaurant data...");
                                seedRestaurants(admin);
                                log.info("Restaurant data seeded!");
                        }

                        // Client with Orders
                        User client = userRepository.findByEmail("client@test.com").orElse(null);
                        if (client == null) {
                                log.info("Creating default CLIENT user...");
                                client = new User();
                                client.setEmail("client@test.com");
                                client.setPassword(passwordEncoder.encode("client123"));
                                client.setFullName("John Doe");
                                client.setPhoneNumber("1234567890");
                                client.setRole(User.Role.CLIENT);
                                client.setIsActive(true);
                                client = userRepository.save(client);
                                log.info("Default CLIENT user created!");

                                seedOrders(client, restaurantRepository.findAll());
                                log.info("Client orders seeded!");
                        }
                };
        }

        private void seedRestaurants(User owner) {
                // Restaurant 1: Sushi
                com.example.foodNow.model.Restaurant sushiPlace = new com.example.foodNow.model.Restaurant();
                sushiPlace.setName("Sushi Master");
                sushiPlace.setBusinessName("Sushi Master Inc.");
                sushiPlace.setAddress("123 Sushi St, Tokyo");
                sushiPlace.setDescription("Authentic Japanese Sushi and Sashimi.");
                sushiPlace.setPhone("555-0101");
                sushiPlace.setImageUrl(
                                "https://images.unsplash.com/photo-1579871494447-9811cf80d66c?w=500&auto=format&fit=crop&q=60");
                sushiPlace.setOwner(owner);
                sushiPlace = restaurantRepository.save(sushiPlace);

                sushiPlace = restaurantRepository.save(sushiPlace);

                com.example.foodNow.model.MenuItem nigiri = createMenuItem(sushiPlace, "Salmon Nigiri",
                                "Fresh salmon on vinegared rice",
                                new java.math.BigDecimal("5.50"),
                                "https://images.unsplash.com/photo-1611143669185-af224c5e3252?w=500&auto=format&fit=crop&q=60",
                                "Sushi");
                createMenuItem(sushiPlace, "Tuna Roll", "Tuna wrapper in seaweed", new java.math.BigDecimal("6.00"),
                                "https://images.unsplash.com/photo-1579584425555-c3ce17fd43fb?w=500&auto=format&fit=crop&q=60",
                                "Sushi");
                createMenuItem(sushiPlace, "Miso Soup", "Traditional soup", new java.math.BigDecimal("3.00"),
                                "https://images.unsplash.com/photo-1547592180-85f173990554?w=500&auto=format&fit=crop&q=60",
                                "Sides");

                // Options for Sushi
                com.example.foodNow.model.MenuOptionGroup sushiOptions = new com.example.foodNow.model.MenuOptionGroup();
                sushiOptions.setName("Wasabi");
                sushiOptions.setRequired(false);
                sushiOptions.setMultiple(false);
                sushiOptions.setMenuItem(nigiri);

                com.example.foodNow.model.MenuOption extraWasabi = new com.example.foodNow.model.MenuOption();
                extraWasabi.setName("Extra Wasabi");
                extraWasabi.setExtraPrice(new java.math.BigDecimal("0.50"));
                extraWasabi.setOptionGroup(sushiOptions);

                sushiOptions.setOptions(java.util.List.of(extraWasabi));
                nigiri.setOptionGroups(java.util.List.of(sushiOptions));
                menuItemRepository.save(nigiri); // Save updates

                // Restaurant 2: Burger
                com.example.foodNow.model.Restaurant burgerJoint = new com.example.foodNow.model.Restaurant();
                burgerJoint.setName("Burger Kingpin");
                burgerJoint.setBusinessName("Burger Kingpin LLC");
                burgerJoint.setAddress("456 Burger Ave, New York");
                burgerJoint.setDescription("Juicy burgers and crispy fries.");
                burgerJoint.setPhone("555-0102");
                burgerJoint.setImageUrl(
                                "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&auto=format&fit=crop&q=60");
                burgerJoint.setOwner(owner);
                burgerJoint = restaurantRepository.save(burgerJoint);

                createMenuItem(burgerJoint, "Classic Cheeseburger", "Beef patty with cheddar cheese",
                                new java.math.BigDecimal("12.99"),
                                "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=500&auto=format&fit=crop&q=60",
                                "Burgers");
                createMenuItem(burgerJoint, "Fries", "Crispy golden fries", new java.math.BigDecimal("4.50"),
                                "https://images.unsplash.com/photo-1573080496987-8198cb147d6d?w=500&auto=format&fit=crop&q=60",
                                "Sides");

                // Restaurant 3: Pizza
                com.example.foodNow.model.Restaurant pizzaPlace = new com.example.foodNow.model.Restaurant();
                pizzaPlace.setName("Pizza Paradise");
                pizzaPlace.setBusinessName("Pizza Paradise Ltd");
                pizzaPlace.setAddress("789 Pizza Blvd, Rome");
                pizzaPlace.setDescription("Wood-fired pizzas with fresh ingredients.");
                pizzaPlace.setPhone("555-0103");
                pizzaPlace.setImageUrl(
                                "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500&auto=format&fit=crop&q=60");
                pizzaPlace.setOwner(owner);
                pizzaPlace = restaurantRepository.save(pizzaPlace);

                createMenuItem(pizzaPlace, "Margherita", "Tomato sauce, mozzarella, and basil",
                                new java.math.BigDecimal("14.00"),
                                "https://images.unsplash.com/photo-1574071318508-1cdbab80d002?w=500&auto=format&fit=crop&q=60",
                                "Pizza");
                createMenuItem(pizzaPlace, "Pepperoni", "Spicy pepperoni on cheese pizza",
                                new java.math.BigDecimal("16.00"),
                                "https://images.unsplash.com/photo-1628840042765-356cda07504e?w=500&auto=format&fit=crop&q=60",
                                "Pizza");

                // Restaurant 4: Tacos (New feature demo)
                com.example.foodNow.model.Restaurant tacoPlace = new com.example.foodNow.model.Restaurant();
                tacoPlace.setName("Taco Fiesta");
                tacoPlace.setBusinessName("Taco Fiesta SA");
                tacoPlace.setAddress("404 Guacamole Ln, Mexico City");
                tacoPlace.setDescription("Authentic Tacos.");
                tacoPlace.setPhone("555-0104");
                tacoPlace.setImageUrl(
                                "https://images.unsplash.com/photo-1551504734-5ee1c4a1479b?w=500&auto=format&fit=crop&q=60");
                tacoPlace.setOwner(owner);
                tacoPlace = restaurantRepository.save(tacoPlace);

                com.example.foodNow.model.MenuItem taco = createMenuItem(tacoPlace, "Beef Tacos",
                                "3 Beef Tacos with herbs",
                                new java.math.BigDecimal("9.50"),
                                "https://images.unsplash.com/photo-1551504734-5ee1c4a1479b?w=500&auto=format&fit=crop&q=60",
                                "Tacos");

                // Options for Tacos
                com.example.foodNow.model.MenuOptionGroup sauceGroup = new com.example.foodNow.model.MenuOptionGroup();
                sauceGroup.setName("Sauce (Choose 1)");
                sauceGroup.setRequired(true);
                sauceGroup.setMultiple(false);
                sauceGroup.setMenuItem(taco);

                com.example.foodNow.model.MenuOption mild = new com.example.foodNow.model.MenuOption();
                mild.setName("Mild");
                mild.setExtraPrice(java.math.BigDecimal.ZERO);
                mild.setOptionGroup(sauceGroup);

                com.example.foodNow.model.MenuOption spicy = new com.example.foodNow.model.MenuOption();
                spicy.setName("Spicy (+0.50)");
                spicy.setExtraPrice(new java.math.BigDecimal("0.50"));
                spicy.setOptionGroup(sauceGroup);

                sauceGroup.setOptions(java.util.List.of(mild, spicy));
                taco.setOptionGroups(java.util.List.of(sauceGroup));
                menuItemRepository.save(taco);

        }

        private com.example.foodNow.model.MenuItem createMenuItem(com.example.foodNow.model.Restaurant restaurant,
                        String name, String desc,
                        java.math.BigDecimal price, String img, String category) {
                com.example.foodNow.model.MenuItem item = new com.example.foodNow.model.MenuItem();
                item.setRestaurant(restaurant);
                item.setName(name);
                item.setDescription(desc);
                item.setPrice(price);
                item.setImageUrl(img);
                item.setCategory(category);
                item.setCategory(category);
                return menuItemRepository.save(item);
        }

        private void seedOrders(User client, java.util.List<com.example.foodNow.model.Restaurant> restaurants) {
                if (restaurants.isEmpty())
                        return;

                java.time.LocalDateTime now = java.time.LocalDateTime.now();
                com.example.foodNow.model.Restaurant sushiPlace = restaurants.stream()
                                .filter(r -> r.getName().contains("Sushi")).findFirst().orElse(restaurants.get(0));
                com.example.foodNow.model.Restaurant burgerPlace = restaurants.stream()
                                .filter(r -> r.getName().contains("Burger")).findFirst().orElse(restaurants.get(0));

                // Order 1: 2 days ago, Sushi
                createOrder(client, sushiPlace, com.example.foodNow.model.Order.OrderStatus.DELIVERED, now.minusDays(2),
                                new java.math.BigDecimal("11.50"));

                // Order 2: 1 day ago, Burger
                createOrder(client, burgerPlace, com.example.foodNow.model.Order.OrderStatus.DELIVERED,
                                now.minusDays(1),
                                new java.math.BigDecimal("17.49"));

                // Order 3: 5 hours ago, Sushi
                createOrder(client, sushiPlace, com.example.foodNow.model.Order.OrderStatus.DELIVERED,
                                now.minusHours(5),
                                new java.math.BigDecimal("6.00"));

                // Order 4: 30 mins ago, Burger (Active)
                createOrder(client, burgerPlace, com.example.foodNow.model.Order.OrderStatus.PREPARING,
                                now.minusMinutes(30),
                                new java.math.BigDecimal("12.99"));
        }

        private void createOrder(User client, com.example.foodNow.model.Restaurant restaurant,
                        com.example.foodNow.model.Order.OrderStatus status, java.time.LocalDateTime createdAt,
                        java.math.BigDecimal total) {

                com.example.foodNow.model.Order order = new com.example.foodNow.model.Order();
                order.setClient(client);
                order.setRestaurant(restaurant);
                order.setStatus(status);
                order.setTotalAmount(total);
                order.setCreatedAt(createdAt);
                order.setUpdatedAt(createdAt);
                order.setDeliveryAddress("123 Client St, City");

                orderRepository.save(order);
        }
}
