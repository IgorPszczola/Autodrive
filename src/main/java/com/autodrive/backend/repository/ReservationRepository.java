package com.autodrive.backend.repository;

import com.autodrive.backend.model.Reservation;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
    
    long countByUserIdAndStatusNot(Integer userId, String status);

    List<Reservation> findByCarUnitId(Long carUnitId);

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.carUnit.id = :carUnitId " +
           "AND :startDate <= r.endDate AND :endDate >= r.startDate")
    boolean existsCollision(
        @Param("carUnitId") Integer carUnitId, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );

    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.carModel.id = :modelId " +
       "AND r.status != 'CANCELLED' " +
       "AND (:startDate < r.endDate AND :endDate > r.startDate)")
    long countOverlappingReservations(
        @Param("modelId") Integer modelId, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate
    );

    List<Reservation> findByUserEmailOrderByCreatedAtDesc(String email);

    boolean existsByCarModelIdAndUserEmailAndStatus(Integer carModelId, String userEmail, String status);
}
