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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class LivreurService {

    private final LivreurRepository livreurRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public LivreurResponse createLivreur(LivreurRequest request) {
        // Only ADMIN can create a livreur
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new AccessDeniedException("Only ADMIN can create a livreur");
        }
        // Admin provides user details in the request. Create a new User with LIVREUR
        // role.
        if (userRepository.existsByEmail(request.getUserEmail())) {
            throw new IllegalArgumentException("A user with the provided email already exists");
        }
        if (request.getUserPassword() == null || request.getUserPassword().isEmpty()) {
            throw new IllegalArgumentException("User password is required");
        }

        User user = new User();
        user.setEmail(request.getUserEmail());
        user.setPassword(passwordEncoder.encode(request.getUserPassword()));
        user.setFullName(request.getUserFullName());
        user.setPhoneNumber(request.getUserPhoneNumber());
        user.setRole(User.Role.LIVREUR);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        if (livreurRepository.findByUserId(savedUser.getId()).isPresent()) {
            throw new IllegalArgumentException("Livreur profile already exists for this user");
        }

        Livreur livreur = new Livreur();
        livreur.setUser(savedUser);
        livreur.setVehicleType(Livreur.VehicleType.valueOf(request.getVehicleType().toUpperCase()));
        livreur.setIsActive(true);
        livreur.setIsAvailable(false);

        Livreur savedLivreur = livreurRepository.save(livreur);

        String subject = "Bienvenue sur FoodNow - Compte Livreur";
        String body = "Bonjour " + savedUser.getFullName() + ",\n\n" +
                "Votre compte Livreur a été créé avec succès.\n" +
                "Voici vos identifiants :\n" +
                "Email : " + savedUser.getEmail() + "\n" +
                "Mot de passe : " + request.getUserPassword() + "\n\n" +
                "Cordialement,\nL'équipe Admin";

        emailService.sendSimpleMessage(savedUser.getEmail(), subject, body);

        return mapToResponse(savedLivreur);
    }

    @Transactional
    public LivreurResponse updateLivreur(Long id, LivreurRequest request) {
        Livreur livreur = livreurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Livreur not found with id: " + id));
        // Only ADMIN or the livreur owner can update the profile
        User currentUser = getCurrentUser();
        if (currentUser.getRole() != User.Role.ADMIN) {
            // if not admin, must be the same livreur - verify using getMyLivreurProfile
            Livreur myProfile = getMyLivreurProfile();
            if (!myProfile.getId().equals(livreur.getId())) {
                throw new AccessDeniedException("You are not authorized to update this livreur");
            }
        }

        livreur.setVehicleType(Livreur.VehicleType.valueOf(request.getVehicleType().toUpperCase()));

        // Update User details
        User user = livreur.getUser();
        boolean userUpdated = false;

        if (request.getUserFullName() != null && !request.getUserFullName().isEmpty()) {
            user.setFullName(request.getUserFullName());
            userUpdated = true;
        }
        if (request.getUserPhoneNumber() != null) {
            user.setPhoneNumber(request.getUserPhoneNumber());
            userUpdated = true;
        }

        if (request.getUserPassword() != null && !request.getUserPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getUserPassword()));
            userUpdated = true;
        }

        if (userUpdated) {
            userRepository.save(user);
        }

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
        System.out.println("DEBUG: toggleAvailability called");
        Livreur livreur = getMyLivreurProfile();
        System.out.println(
                "DEBUG: Found profile for: " + livreur.getId() + ", Current Status: " + livreur.getIsAvailable());
        boolean newState = !livreur.getIsAvailable();
        livreur.setIsAvailable(newState);
        livreurRepository.save(livreur);
        System.out.println("DEBUG: availability toggled. New Status: " + newState);
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

    private LivreurResponse mapToResponse(Livreur livreur) {
        LivreurResponse response = new LivreurResponse();
        response.setId(livreur.getId());
        response.setUserId(livreur.getUser().getId());
        response.setFullName(livreur.getUser().getFullName());
        response.setEmail(livreur.getUser().getEmail());
        response.setPhone(livreur.getUser().getPhoneNumber());
        response.setVehicleType(livreur.getVehicleType().name());
        response.setIsAvailable(livreur.getIsAvailable());
        response.setIsActive(livreur.getIsActive());
        response.setLatitude(livreur.getCurrentLatitude());
        response.setLongitude(livreur.getCurrentLongitude());

        Double avg = 0.0;
        if (livreur.getRatingCount() != null && livreur.getRatingCount() > 0) {
            avg = livreur.getRatingSum() / livreur.getRatingCount();
            // Round to 1 decimal
            avg = Math.round(avg * 10.0) / 10.0;
        }
        response.setAverageRating(avg);
        response.setCompletedDeliveries(
                livreur.getCompletedDeliveries() == null ? 0 : livreur.getCompletedDeliveries());

        return response;
    }
}
