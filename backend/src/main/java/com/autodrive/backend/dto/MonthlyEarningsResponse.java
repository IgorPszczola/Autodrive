package com.autodrive.backend.dto;

import java.math.BigDecimal;

public record MonthlyEarningsResponse(
    int month,
    String monthName,
    BigDecimal earnings
) {}
