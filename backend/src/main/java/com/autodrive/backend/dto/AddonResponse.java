package com.autodrive.backend.dto;

import java.math.BigDecimal;

public record AddonResponse(
    Integer id,
    String name,
    String description,
    BigDecimal pricePerDay
) {
    
}
