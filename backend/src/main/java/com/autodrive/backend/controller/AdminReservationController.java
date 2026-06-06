package com.autodrive.backend.controller;

import com.autodrive.backend.dto.CarRequestReturn;
import com.autodrive.backend.dto.CarReturnResponse;
import com.autodrive.backend.model.Reservation;
import com.autodrive.backend.model.ReturnReport;
import com.autodrive.backend.service.ReservationService;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PutMapping("/{reservationId}/assign-unit")
    public ResponseEntity<String> assignUnit(@PathVariable Integer reservationId) {
        reservationService.assignFirstAvailableCarUnit(reservationId);
        return ResponseEntity.ok("System assigned a car unit to the reservation.");
    }

    @PutMapping("/{id}/return")
    public ResponseEntity<?> returnCar(@PathVariable Integer id, @RequestBody CarRequestReturn request) {
        try {
            CarReturnResponse updatedReservation = reservationService.processCarReturn(id, request);
            return ResponseEntity.ok(Map.of(
                "status", "COMPLETED",
                "message", "Car returned successfully!",
                "distanceTraveledKm", updatedReservation.distanceTraveledKm(),
                "finalTotalPrice", updatedReservation.finalTotalPrice()
            ));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/{reservationId}/return-report")
    public ResponseEntity<ReturnReport> getReturnReport(@PathVariable Integer reservationId) {
        // Zakładam, że masz taką metodę w serwisie lub repozytorium:
        ReturnReport report = reservationService.getReturnReportByReservationId(reservationId);
        return ResponseEntity.ok(report);
    }
}