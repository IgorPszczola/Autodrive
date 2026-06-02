package com.autodrive.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ReservationRequest(
    Integer id,
    LocalDateTime startDate,
    LocalDateTime endDate,
    List<Integer> addonIds
) {}
