package com.autodrive.backend.model;

import java.math.BigDecimal;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "addons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Addon {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer addon_id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "price_per_day", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerDay;

    @Column(columnDefinition = "TEXT")
    private String description;

}
