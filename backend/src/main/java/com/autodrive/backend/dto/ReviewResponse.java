package com.autodrive.backend.dto;

import java.time.LocalDateTime;

public record ReviewResponse(
    Integer id,
    Integer carModelId,
    String carModelName,
    String userEmail,
    Integer rating,
    String comment,
    LocalDateTime createdAt
) {
    
}
