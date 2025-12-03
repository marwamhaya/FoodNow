package com.example.foodNow.controller;

import com.example.foodNow.dto.LivreurRequest;
import com.example.foodNow.dto.LivreurResponse;
import com.example.foodNow.dto.LocationUpdateDto;
import com.example.foodNow.service.LivreurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/livreurs")
@RequiredArgsConstructor
public class LivreurController {

    private final LivreurService livreurService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LivreurResponse> createLivreur(@Valid @RequestBody LivreurRequest request) {
        return new ResponseEntity<>(livreurService.createLivreur(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LivreurResponse> updateLivreur(
            @PathVariable Long id,
            @Valid @RequestBody LivreurRequest request) {
        return ResponseEntity.ok(livreurService.updateLivreur(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleActivity(@PathVariable Long id) {
        livreurService.toggleActivity(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/availability")
    @PreAuthorize("hasRole('LIVREUR')")
    public ResponseEntity<Void> toggleAvailability() {
        livreurService.toggleAvailability();
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/location")
    @PreAuthorize("hasRole('LIVREUR')")
    public ResponseEntity<Void> updateLocation(@Valid @RequestBody LocationUpdateDto location) {
        livreurService.updateLocation(location);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LivreurResponse>> getAllLivreurs() {
        return ResponseEntity.ok(livreurService.getAllLivreurs());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LivreurResponse> getLivreurById(@PathVariable Long id) {
        return ResponseEntity.ok(livreurService.getLivreurById(id));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('LIVREUR')")
    public ResponseEntity<LivreurResponse> getMyProfile() {
        return ResponseEntity.ok(livreurService.getMyProfile());
    }
}
