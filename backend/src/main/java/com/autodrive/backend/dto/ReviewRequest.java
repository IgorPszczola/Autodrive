package com.autodrive.backend.dto;

public record ReviewRequest(
    Integer rating,
    String comment,
    Integer carModelId
) {
}
