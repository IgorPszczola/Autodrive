package com.autodrive.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autodrive.backend.dto.CarModelResponse;
import com.autodrive.backend.service.CarModelService;

@RestController
@RequestMapping("/api/cars")
public class CarController {
    
    private final CarModelService carModelService;

    public CarController(CarModelService carModelService) {
        this.carModelService = carModelService;
    }

    @GetMapping("/models")
    public List<CarModelResponse> allCarModels() {
        return carModelService.allCarModels();
    }

    @GetMapping("/models/{id}")
    public ResponseEntity<CarModelResponse> getCarModelById(@PathVariable Integer id){
        CarModelResponse response = carModelService.getCarModelById(id);
        if (response != null) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
