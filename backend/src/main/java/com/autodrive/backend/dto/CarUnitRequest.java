package com.autodrive.backend.dto;

public record CarUnitRequest(
    Integer carModelId,
    String licensePlate,
    String vin,
    Long currentMileage,
    String color,
    Integer productionYear,
    String imageUrl
) {
    
}
