package com.autodrive.backend.dto;

import java.math.BigDecimal;

public record CarModelResponse(
    Integer carModelId,
    String brand,
    String model,
    String segment,
    Integer productionYear,
    BigDecimal pricePerDay,
    BigDecimal depositAmount,
    Integer mileageLimitPerDay,
    BigDecimal extraMileageFee,
    String imageUrl,
    Integer powerHp,
    String transmissionType,
    String fuelType
) {}