package com.autodrive.backend.controller;

import com.autodrive.backend.dto.ReviewRequest;
import com.autodrive.backend.dto.ReviewResponse;
import com.autodrive.backend.service.ReviewService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest request, Principal principal) {
        try {
        ReviewResponse response = reviewService.addReview(request, principal.getName());
        return ResponseEntity.ok(response);
    } catch (IllegalStateException | IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(java.util.Map.of(
            "status", 400,
            "error", e.getMessage()
        ));
    }
    }

    @GetMapping("/model/{carModelId}")
    public ResponseEntity<List<ReviewResponse>> getCarModelReviews(@PathVariable Integer carModelId) {
        List<ReviewResponse> reviews = reviewService.getReviewsForModel(carModelId);
        return ResponseEntity.ok(reviews);
    }
}