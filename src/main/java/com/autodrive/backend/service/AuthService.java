package com.autodrive.backend.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.autodrive.backend.dto.LoginRequest;
import com.autodrive.backend.dto.RegisterRequest;
import com.autodrive.backend.model.User;
import com.autodrive.backend.repository.UserRepository;
import com.autodrive.backend.security.JwtUtils;


@Service
public class AuthService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtils jwtUtils, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    public String registerUser(RegisterRequest request){

        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("User already exists");
        }
        User user = new User();
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setPhoneNumber(request.phoneNumber());
        user.setDriverLicenseNumber(request.driverLicenseNumber());

        String encodedPassword = passwordEncoder.encode(request.password());
        user.setPasswordHash(encodedPassword);

        userRepository.save(user);

        return "User registered successfully";
    }

    public String loginUser(LoginRequest request){
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        return jwtUtils.generateJwtToken(request.email());
    }
}

