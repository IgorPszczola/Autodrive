package com.autodrive.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.autodrive.backend.dto.ReviewRequest;
import com.autodrive.backend.dto.ReviewResponse;
import com.autodrive.backend.model.CarModel;
import com.autodrive.backend.model.Review;
import com.autodrive.backend.model.User;
import com.autodrive.backend.repository.CarModelRepository;
import com.autodrive.backend.repository.ReservationRepository;
import com.autodrive.backend.repository.ReviewRepository;
import com.autodrive.backend.repository.UserRepository;

@Service
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;
    private final CarModelRepository carModelRepository;
    private final UserRepository userRepository;

    public ReviewService(ReviewRepository reviewRepository, ReservationRepository reservationRepository,
                         CarModelRepository carModelRepository, UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.reservationRepository = reservationRepository;
        this.carModelRepository = carModelRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public ReviewResponse addReview(ReviewRequest request, String email) {

        boolean hasCompletedRent = reservationRepository.existsByCarModelIdAndUserEmailAndStatus(
                request.carModelId(), email, "COMPLETED"
        );

        if (!hasCompletedRent) {
            throw new IllegalStateException("You cannot post a review for a model you haven't rented and returned.");
        }

        if (request.rating() < 1 || request.rating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }

        CarModel carModel = carModelRepository.findById(request.carModelId())
                .orElseThrow(() -> new RuntimeException("Car model not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Review review = new Review();
        review.setCarModel(carModel);
        review.setUser(user);
        review.setRating(request.rating());
        review.setComment(request.comment());

        Review savedReview = reviewRepository.save(review);

        String fullModelName = carModel.getBrand() + " " + carModel.getModel();

        return new ReviewResponse(
                savedReview.getId(),
                carModel.getId(),
                fullModelName,
                user.getEmail(),
                savedReview.getRating(),
                savedReview.getComment(),
                savedReview.getCreatedAt()
        );
    }

    public List<ReviewResponse> getReviewsForModel(Integer carModelId) {
        return reviewRepository.findByCarModelIdOrderByCreatedAtDesc(carModelId).stream()
                .map(r -> {
                    String fullModelName = r.getCarModel().getBrand() + " " + r.getCarModel().getModel();
                    return new ReviewResponse(
                            r.getId(),
                            r.getCarModel().getId(),
                            fullModelName,
                            r.getUser().getEmail(),
                            r.getRating(),
                            r.getComment(),
                            r.getCreatedAt()
                    );
                }).toList();
    }
}
