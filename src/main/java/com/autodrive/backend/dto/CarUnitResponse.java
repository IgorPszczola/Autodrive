package com.autodrive.backend.dto;

public record CarUnitResponse(
    Long id,
    Integer modelId,
    String vin,
    String licensePlate,
    String color,
    String status
) {}
