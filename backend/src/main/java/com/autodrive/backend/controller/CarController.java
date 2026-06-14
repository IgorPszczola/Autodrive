package com.autodrive.backend.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.autodrive.backend.dto.CarModelResponse;
import com.autodrive.backend.dto.CarUnitResponse;
import com.autodrive.backend.dto.TerminResponse;
import com.autodrive.backend.model.CarModel;
import com.autodrive.backend.service.CarModelService;
import com.autodrive.backend.service.CarUnitService;

@RestController
@RequestMapping("/api/cars")
public class CarController {
    
    private final CarModelService carModelService;
    private final CarUnitService carUnitService;

    public CarController(CarModelService carModelService, CarUnitService carUnitService) {
        this.carModelService = carModelService;
        this.carUnitService = carUnitService;
    }

    @GetMapping("/models")
    public ResponseEntity<?> getModels(
            @PageableDefault(size = 10, page = 0) Pageable pageable,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String fuelType,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String segment,
            @RequestParam(defaultValue = "pricePerDay") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        try {

            Page<CarModelResponse> modelsPage = carModelService.getFilteredModels(pageable, brand, fuelType, maxPrice, segment, sortBy, sortDir);

            return ResponseEntity.ok(modelsPage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/models/{id}")
    public ResponseEntity<?> getCarModelById(@PathVariable Integer id){
        try {
            CarModelResponse response = carModelService.getCarModelById(id);
            if (response != null) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(404).body(Map.of(
                    "status", 404,
                    "error", "Car model not found with id: " + id
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/models/{id}/units")
    public ResponseEntity<?> getCarUnitsByModelId(@PathVariable Integer id){
        try {
            List<CarUnitResponse> units = carUnitService.getCarUnitsByModelId(id);
            return ResponseEntity.ok(units);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", e.getMessage()
            ));
        }
    }

    @GetMapping("/units/{carUnitId}/occupied-dates")
    public ResponseEntity<?> getCarOccupiedDates(@PathVariable Long carUnitId) {
        try {
            List<TerminResponse> dates = carUnitService.getOccupiedDatesForCar(carUnitId);
            return ResponseEntity.ok(dates);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", e.getMessage()
            ));
        }
    }
}