package com.example.foodNow.service;

import com.example.foodNow.dto.LivreurRequest;
import com.example.foodNow.dto.LivreurResponse;
import com.example.foodNow.dto.LocationUpdateDto;
import com.example.foodNow.exception.ResourceNotFoundException;
import com.example.foodNow.model.Livreur;
import com.example.foodNow.model.User;
import com.example.foodNow.repository.LivreurRepository;
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
public class LivreurService {

    private final LivreurRepository livreurRepository;
    private final UserRepository userRepository;

    @Transactional
    public LivreurResponse createLivreur(LivreurRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        if (user.getRole() != User.Role.LIVREUR) {
            throw new IllegalArgumentException("User must have LIVREUR role");
        }

        if (livreurRepository.findByUserId(user.getId()).isPresent()) {
            throw new IllegalArgumentException("Livreur profile already exists for this user");
        }

        Livreur livreur = new Livreur();
        livreur.setUser(user);
        livreur.setVehicleType(Livreur.VehicleType.valueOf(request.getVehicleType().toUpperCase()));
        livreur.setIsActive(true);
        livreur.setIsAvailable(false);

        Livreur savedLivreur = livreurRepository.save(livreur);
        return mapToResponse(savedLivreur);
    }

    @Transactional
    public LivreurResponse updateLivreur(Long id, LivreurRequest request) {
        Livreur livreur = livreurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur not found with id: " + id));

        livreur.setVehicleType(Livreur.VehicleType.valueOf(request.getVehicleType().toUpperCase()));

        Livreur updatedLivreur = livreurRepository.save(livreur);
        return mapToResponse(updatedLivreur);
    }

    @Transactional
    public void toggleActivity(Long id) {
        Livreur livreur = livreurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur not found with id: " + id));
        livreur.setIsActive(!livreur.getIsActive());
        livreurRepository.save(livreur);
    }

    @Transactional
    public void toggleAvailability() {
        Livreur livreur = getMyLivreurProfile();
        livreur.setIsAvailable(!livreur.getIsAvailable());
        livreurRepository.save(livreur);
    }

    @Transactional
    public void updateLocation(LocationUpdateDto location) {
        Livreur livreur = getMyLivreurProfile();
        livreur.setCurrentLatitude(location.getLatitude());
        livreur.setCurrentLongitude(location.getLongitude());
        livreurRepository.save(livreur);
    }

    public LivreurResponse getLivreurById(Long id) {
        Livreur livreur = livreurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur not found with id: " + id));
        return mapToResponse(livreur);
    }

    public LivreurResponse getMyProfile() {
        return mapToResponse(getMyLivreurProfile());
    }

    public List<LivreurResponse> getAllLivreurs() {
        return livreurRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private Livreur getMyLivreurProfile() {
        User currentUser = getCurrentUser();
        return livreurRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Livreur profile not found for current user"));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private LivreurResponse mapToResponse(Livreur livreur) {
        LivreurResponse response = new LivreurResponse();
        response.setId(livreur.getId());
        response.setUserId(livreur.getUser().getId());
        response.setFullName(livreur.getUser().getFullName());
        response.setPhone(livreur.getUser().getPhoneNumber());
        response.setVehicleType(livreur.getVehicleType().name());
        response.setIsAvailable(livreur.getIsAvailable());
        response.setIsActive(livreur.getIsActive());
        response.setLatitude(livreur.getCurrentLatitude());
        response.setLongitude(livreur.getCurrentLongitude());
        return response;
    }
}
