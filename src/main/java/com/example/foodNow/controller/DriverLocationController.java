package com.example.foodNow.controller;

import com.example.foodNow.dto.LocationUpdateDto;
import com.example.foodNow.service.DriverLocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/driver-locations")
@RequiredArgsConstructor
public class DriverLocationController {

    private final DriverLocationService locationService;
    private final SimpMessagingTemplate messagingTemplate;

    @PatchMapping
    @PreAuthorize("hasRole('LIVREUR')")
    public ResponseEntity<Void> updateLocation(@RequestBody LocationUpdateDto locationDto) {
        var location = locationService.updateLocation(locationDto);
        // Broadcast to /topic/drivers/{driverId}
        messagingTemplate.convertAndSend("/topic/drivers/" + location.getDriver().getId(), location);
        return ResponseEntity.ok().build();
    }
}
