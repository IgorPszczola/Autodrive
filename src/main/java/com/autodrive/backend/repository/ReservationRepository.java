package com.autodrive.backend.repository;

import com.autodrive.backend.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    
    long countByUserIdAndStatus(Integer userId, String status);
}
