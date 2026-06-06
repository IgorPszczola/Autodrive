package com.autodrive.backend.controller;

import java.util.Map;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.autodrive.backend.dto.CarModelRequest;
import com.autodrive.backend.dto.CarModelResponse;
import com.autodrive.backend.dto.CarUnitRequest;
import com.autodrive.backend.dto.CarUnitResponse;
import com.autodrive.backend.service.CarModelService;
import com.autodrive.backend.service.CarUnitService;

@RestController
@RequestMapping("/api/admin")
public class AdminCarController {

    private final CarUnitService carUnitService;
    private final CarModelService carModelService;

    public AdminCarController(CarUnitService carUnitService, CarModelService carModelService) {
        this.carUnitService = carUnitService;
        this.carModelService = carModelService;
    }

    @PutMapping("/units/{id}/status")
    public ResponseEntity<?> updateCarUnitStatus(
            @PathVariable Integer id, 
            @RequestParam String status
    ) {
        try {
            CarUnitResponse response = carUnitService.updateUnitStatus(id, status);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/models")
    public ResponseEntity<?> createCarModel(@RequestBody CarModelRequest request) {
        try {
            CarModelResponse savedModel = carModelService.addCarModel(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedModel);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/units")
    public ResponseEntity<?> createCarUnit(@RequestBody CarUnitRequest request) {
        try {
            CarUnitResponse savedUnit = carUnitService.addCarUnit(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedUnit);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                "status", 400,
                "error", "Failed to create car unit: " + e.getMessage()
            ));
        }
    }
}