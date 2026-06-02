package com.autodrive.backend.dto;

public record LoginRequest(
    String email,
    String password
) {}