package com.autodrive.backend.controller;

import com.autodrive.backend.model.ReturnReport;
import com.autodrive.backend.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/admin/returns")
public class ReturnController {

    private final ReservationService reservationService;

    public ReturnController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReturnReport> createReturnReport(
            @RequestParam Integer reservationId,
            @RequestParam Integer endMileage,
            @RequestParam BigDecimal damageCost,
            @RequestParam(required = false) String damageDescription) {
        
        ReturnReport report = reservationService.processReturn(reservationId, endMileage, damageCost, damageDescription);
        return ResponseEntity.ok(report);
    }
}