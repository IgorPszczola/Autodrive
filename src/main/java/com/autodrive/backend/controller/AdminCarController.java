package com.autodrive.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.autodrive.backend.dto.CarUnitResponse;
import com.autodrive.backend.service.CarUnitService;

@RestController
@RequestMapping("/api/admin")
public class AdminCarController {

    private final CarUnitService carUnitService;

    public AdminCarController(CarUnitService carUnitService) {
        this.carUnitService = carUnitService;
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
}
