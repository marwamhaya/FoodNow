package com.example.foodNow.service;

import com.example.foodNow.dto.LocationUpdateDto;
import com.example.foodNow.model.DriverLocation;
import com.example.foodNow.model.User;
import com.example.foodNow.repository.DriverLocationRepository;
import com.example.foodNow.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverLocationService {

    private final DriverLocationRepository locationRepository;
    private final UserRepository userRepository;

    @Transactional
    public DriverLocation updateLocation(LocationUpdateDto locationDto) {
        User currentUser = getCurrentUser();

        DriverLocation location = new DriverLocation();
        location.setDriver(currentUser);
        location.setLatitude(java.math.BigDecimal.valueOf(locationDto.getLatitude()));
        location.setLongitude(java.math.BigDecimal.valueOf(locationDto.getLongitude()));
        location.setTimestamp(java.time.LocalDateTime.now());

        return locationRepository.save(location);
    }

    public List<DriverLocation> getLocationHistory(Long driverId) {
        return locationRepository.findByDriverId(driverId);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String principal = authentication.getName();
        try {
            Long userId = Long.parseLong(principal);
            return userRepository.findById(userId).orElseThrow();
        } catch (NumberFormatException e) {
            return userRepository.findByEmail(principal).orElseThrow();
        }
    }
}
