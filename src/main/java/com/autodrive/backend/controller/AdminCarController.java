package com.autodrive.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.autodrive.backend.dto.CarModelRequest;
import com.autodrive.backend.dto.CarModelResponse;
import com.autodrive.backend.dto.CarUnitResponse;
import com.autodrive.backend.service.CarModelService;
import com.autodrive.backend.service.CarUnitService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


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
    public ResponseEntity<?>  updateCarUnitStatus (
            @PathVariable Integer id, 
            @RequestParam String status
    ) {
        try {
            CarUnitResponse response = carUnitService.updateUnitStatus(id, status);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/models")
    public ResponseEntity<?> createCarModel(@RequestBody CarModelRequest request) {
        try {
            CarModelResponse savedModel = carModelService.addCarModel(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedModel);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    

}
