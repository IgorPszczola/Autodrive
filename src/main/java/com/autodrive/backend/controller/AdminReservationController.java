package com.autodrive.backend.controller;

import com.autodrive.backend.service.ReservationService;
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
}