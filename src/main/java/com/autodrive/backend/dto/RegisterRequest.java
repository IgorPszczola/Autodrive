package com.autodrive.backend.dto;

public record RegisterRequest(
    String email,
    String password,
    String firstName,
    String lastName,
    String driverLicenseNumber,
    String phoneNumber
) {}