package com.autodrive.backend.controller;

import com.autodrive.backend.model.InsuranceVariant;
import com.autodrive.backend.repository.InsuranceVariantRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/insurances")
public class InsuranceController {

    private final InsuranceVariantRepository repository;

    public InsuranceController(InsuranceVariantRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<InsuranceVariant>> getAllVariants() {
        return ResponseEntity.ok(repository.findAll());
    }
}