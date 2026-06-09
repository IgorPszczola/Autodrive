package com.autodrive.backend.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.autodrive.backend.dto.ReservationRequest;
import com.autodrive.backend.dto.ReservationResponse;
import com.autodrive.backend.model.Reservation;
import com.autodrive.backend.service.ReservationService;
import com.autodrive.backend.dto.AddonResponse;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<?> createReservation(
            @RequestParam Integer carModelId,
            @RequestBody ReservationRequest request,
            Principal principal
    ) {
        try {
            String email = principal.getName();
            Reservation res = reservationService.makeReservation(carModelId, email, request);
            return ResponseEntity.ok(res);
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", e.getMessage()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "status", 500,
                "error", "Unknown error occurred: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<?> getMyReservations(Principal principal) {
        try {
            List<Reservation> reservations = reservationService.getMyReservations(principal.getName());
            
            List<ReservationResponse> response = reservations.stream().map(res -> new ReservationResponse(
                    res.getId(),
                    res.getStartDate(),
                    res.getEndDate(),
                    res.getBasePrice(),
                    res.getDiscountApplied(),
                    res.getTotalPrice(),
                    res.getCarModel().getDepositAmount(),
                    res.getStatus(),
                    res.getCreatedAt(),
                    res.getUser().getEmail(),
                    res.getCarModel().getBrand(),
                    res.getCarModel().getModel(),
                    res.getCarUnit() != null ? res.getCarUnit().getLicensePlate() : null,
                    res.getInsuranceVariant().getName(),
                    res.getAddons().stream().map(addon -> new AddonResponse(
                        addon.getId(),
                        addon.getName(),
                        addon.getDescription(),
                        addon.getPricePerDay()
                    )).toList()
            )).toList();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", e.getMessage()
            ));
        }
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable Integer id, Principal principal) {
        try {
            reservationService.cancelReservation(id, principal.getName());
            return ResponseEntity.ok(Map.of(
                "status", 200,
                "message", "Reservation canceled successfully."
            ));
        } catch (IllegalStateException | IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", e.getMessage()
            ));
        }
    }
}