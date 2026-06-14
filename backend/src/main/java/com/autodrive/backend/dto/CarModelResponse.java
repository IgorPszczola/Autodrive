package com.autodrive.backend.dto;

import java.math.BigDecimal;

public record CarModelResponse(
    Integer id,
    String brand,
    String model,
    String segment,
    BigDecimal pricePerDay,
    BigDecimal depositAmount,
    Integer mileageLimitPerDay,
    BigDecimal extraMileageFee,
    Integer powerHp,
    String transmissionType,
    String fuelType,
    Integer minRentDays
) {}