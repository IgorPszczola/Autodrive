package com.autodrive.backend.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "return_reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReturnReport {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "return_date")
    private LocalDate returnDate;
    
    @Column(name = "end_mileage")
    private Integer endMileage;
    
    @Column(name = "extra_mileage_cost")
    private Double extraMileageCost;
    
    @Column(name = "damage_cost")
    private Double damageCost;
    
    @Column(name = "damage_description", length = 1000)
    private String damageDescription;
    
    @Column(name = "total_surcharge")
    private Double totalSurcharge;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;
}
