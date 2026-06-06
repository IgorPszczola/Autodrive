package com.autodrive.backend.dto;

import java.time.LocalDate;

public record TerminResponse(
    LocalDate startDate,
    LocalDate endDate
) {
    
}
