package com.example.foodNow.controller;

import com.example.foodNow.model.User;
import com.example.foodNow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(userService.getUser(authentication.getName()));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            Authentication authentication,
            @RequestBody Map<String, String> payload) {
        String newPassword = payload.get("newPassword");
        userService.changePassword(authentication.getName(), newPassword);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAccount(Authentication authentication) {
        userService.deleteUser(authentication.getName());
        return ResponseEntity.ok().build();
    }
}
