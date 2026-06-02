package com.autodrive.backend.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CarModelResponse {
    private Integer car_model_id;
    private String brand;
    private String model;
    private Integer productionYear;
    private BigDecimal pricePerDay;
}
