package com.autodrive.backend.repository;

import com.autodrive.backend.model.Review;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findByCarModelIdOrderByCreatedAtDesc(Integer carModelId);
}
