package com.autodrive.backend.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autodrive.backend.dto.AddonResponse;
import com.autodrive.backend.repository.AddonRepository;


@RestController
@RequestMapping("/api/addons")
public class AddonController {
    private final AddonRepository addonRepository;

    public AddonController(AddonRepository addonRepository) {
        this.addonRepository = addonRepository;
    }

    @GetMapping
    public List<AddonResponse> getAllAddons() {
        return addonRepository.findAll().stream()
            .map(addon -> new AddonResponse(
                addon.getId(),
                addon.getName(),
                addon.getDescription(),
                addon.getPricePerDay()
            ))
            .toList();
    }
    
}
