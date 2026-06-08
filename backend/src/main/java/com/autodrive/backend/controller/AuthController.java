package com.autodrive.backend.controller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autodrive.backend.dto.LoginRequest;
import com.autodrive.backend.dto.RegisterRequest;
import com.autodrive.backend.model.User;
import com.autodrive.backend.model.Role;
import com.autodrive.backend.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request){
        try {
            String result = authService.registerUser(request);
            return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", result
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            String token = authService.loginUser(request);
            return ResponseEntity.ok(Map.of(
                "status", 200,
                "token", token
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", e.getMessage()
            ));
        }
    }

    @CrossOrigin(origins = "*")
    @GetMapping("/me")
    public ResponseEntity<?> getLoggedUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Niezalogowany użytkownik"));
        }
        
        User user = authService.findByEmail(userDetails.getUsername());
        
        String userRole = user.getRoles().stream()
                            .map(Role::getName) 
                            .findFirst()
                            .orElse("ROLE_USER");
        
        return ResponseEntity.ok(Map.of(
            "id", user.getId(),
            "email", user.getEmail(),
            "firstName", user.getFirstName(),
            "lastName", user.getLastName(),
            "role", userRole,
            "isActive", user.isActive()
        ));
    }
}