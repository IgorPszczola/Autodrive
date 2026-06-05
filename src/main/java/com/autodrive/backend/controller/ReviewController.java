package com.autodrive.backend.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autodrive.backend.dto.ReviewRequest;
import com.autodrive.backend.dto.ReviewResponse;
import com.autodrive.backend.model.Review;
import com.autodrive.backend.service.ReviewService;

@RestController
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }


    @GetMapping("/models/{id}/reviews")
   public ResponseEntity<List<ReviewResponse>> getCarModelReviews(@PathVariable Integer id) {
        List<Review> reviews = reviewService.getReviewsForModel(id);
        
        List<ReviewResponse> response = reviews.stream().map(r -> new ReviewResponse(
                r.getId(),
                r.getCarModel().getId(),
                r.getCarModel().getBrand() + " " + r.getCarModel().getModel(),
                r.getUser().getEmail(),
                r.getRating(),
                r.getComment(),
                r.getCreatedAt()
        )).toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reviews")
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest request, Principal principal) {
        try {
            Review review = reviewService.addReview(request, principal.getName());
            
            ReviewResponse response = new ReviewResponse(
                    review.getId(),
                    review.getCarModel().getId(),
                    review.getCarModel().getBrand() + " " + review.getCarModel().getModel(),
                    review.getUser().getEmail(),
                    review.getRating(),
                    review.getComment(),
                    review.getCreatedAt()
            );

            return ResponseEntity.ok(response);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
