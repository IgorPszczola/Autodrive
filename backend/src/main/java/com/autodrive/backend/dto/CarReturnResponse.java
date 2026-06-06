package com.autodrive.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CarReturnResponse(
    Integer reservationId,
    String status,
    LocalDate returnDate,
    Long endMileage,
    Double extraMileageCost,
    Double distanceTraveledKm,
    Double damageCost,
    String damageDescription,
    BigDecimal finalTotalPrice
) {
    
}
