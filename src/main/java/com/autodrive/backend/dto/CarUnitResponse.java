package com.autodrive.backend.dto;

public record CarUnitResponse(
    Integer carUnitId,
    Integer carModelId,
    String vin,
    String licensePlate,
    String color,
    String status
) {}
