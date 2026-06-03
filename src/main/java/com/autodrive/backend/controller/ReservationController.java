package com.autodrive.backend.controller;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.autodrive.backend.dto.ReservationRequest;
import com.autodrive.backend.model.Reservation;
import com.autodrive.backend.service.ReservationService;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<Reservation> createReservation(
            @RequestParam Integer carUnitId,
            @RequestBody ReservationRequest request,
            Principal principal
    ) {
        String email = principal.getName();

        Reservation reservation = reservationService.makeReservation(carUnitId, email, request);

        return ResponseEntity.ok(reservation);
    }
}
