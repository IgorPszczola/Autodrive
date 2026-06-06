package com.autodrive.backend.dto;

import java.math.BigDecimal;

public record CarRequestReturn(
    Long currentMileage,
    boolean isDamaged,
    String damageNotes,
    BigDecimal damageCost
) {
}
