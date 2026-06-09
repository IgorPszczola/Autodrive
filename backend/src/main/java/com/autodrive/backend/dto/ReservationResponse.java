package com.autodrive.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ReservationResponse(
    Integer id,
    LocalDate startDate,
    LocalDate endDate,
    BigDecimal basePrice,
    Double discountApplied,
    BigDecimal totalPrice,
    BigDecimal depositAmount,
    String status,
    LocalDateTime createdAt,
    String userEmail,
    String carBrand,
    String carModel,
    String licensePlate,
    String insuranceVariantName,
    List<AddonResponse> addons
) {}