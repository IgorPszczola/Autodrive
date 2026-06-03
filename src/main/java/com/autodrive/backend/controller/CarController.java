package com.autodrive.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autodrive.backend.dto.CarModelResponse;
import com.autodrive.backend.dto.CarUnitResponse;
import com.autodrive.backend.dto.TerminResponse;
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

    @GetMapping("/models/{id}/units")
    public ResponseEntity<List<CarUnitResponse>> getCarUnitsByModelId(@PathVariable Integer id){
        List<CarUnitResponse> units = carUnitService.getCarUnitsByModelId(id);
        return ResponseEntity.ok(units);
    }

    @GetMapping("/units/{carUnitId}/occupied-dates")
    public ResponseEntity<List<TerminResponse>> getCarOccupiedDates(@PathVariable Long carUnitId) {
        List<TerminResponse> dates = carUnitService.getOccupiedDatesForCar(carUnitId);
        return ResponseEntity.ok(dates);
    }
}
