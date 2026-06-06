package com.autodrive.backend.dto;

import java.math.BigDecimal;

public record DashboardStatsResponse(
    BigDecimal totalEarnings,       // Suma zarobków ze wszystkich potwierdzonych rezerwacji
    long totalReservationsCount,   // Łączna liczba rezerwacji w systemie (oprócz anulowanych)
    long activeRentalsCount,       // Ile aut jest aktualnie w trasie (status RENTED)
    long fleetInRepairCount,       // Ile aut stoi u mechanika (status IN_REPAIR)
    double fleetUtilizationRate    // Procent obłożenia floty (ile % aut zarabia/pracuje)
) {
    
}
