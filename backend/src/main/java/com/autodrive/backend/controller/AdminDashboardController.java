package com.autodrive.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autodrive.backend.dto.DashboardStatsResponse;
import com.autodrive.backend.dto.MonthlyEarningsResponse;
import com.autodrive.backend.service.ReservationService;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final ReservationService reservationService;

    public AdminDashboardController(ReservationService reservationService) {
        this.reservationService = reservationService;

    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsResponse> getStats() {
        DashboardStatsResponse stats = reservationService.getDashboardStats();
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/monthly-earnings")
    public ResponseEntity<List<MonthlyEarningsResponse>> getMonthlyEarnings() {
        List<MonthlyEarningsResponse> monthlyEarnings = reservationService.getMonthlyEarnings();
        return ResponseEntity.ok(monthlyEarnings);
    }
}
