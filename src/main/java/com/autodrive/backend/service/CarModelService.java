package com.autodrive.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.autodrive.backend.repository.CarModelRepository;

import com.autodrive.backend.dto.CarModelResponse;
import com.autodrive.backend.model.CarModel;

@Service
public class CarModelService {
    private final CarModelRepository carModelRepository;

    public CarModelService(CarModelRepository carModelRepository) {
        this.carModelRepository = carModelRepository;
    }

    public List<CarModelResponse> allCarModels(){
        return carModelRepository.findAll().stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    public CarModelResponse getCarModelById(Integer id){
        return carModelRepository.findById(id)
            .map(this::mapToDto)
            .orElse(null);
    }

    public List<CarModel> getFilteredModels(String brand, String fuelType, java.math.BigDecimal maxPrice, String sortBy, String sortDir) {
        String sortField = resolveSortField(sortBy);
        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortField).descending()
                : Sort.by(sortField).ascending();

        return carModelRepository.findFiltered(brand, fuelType, maxPrice, sort);
    }

    private String resolveSortField(String sortBy) {
        if ("year".equalsIgnoreCase(sortBy) || "productionYear".equalsIgnoreCase(sortBy)) {
            return "productionYear";
        }
        if ("brand".equalsIgnoreCase(sortBy)) {
            return "brand";
        }
        return "pricePerDay";
    }

    private CarModelResponse mapToDto(CarModel carModel) {
        return new CarModelResponse(
        carModel.getId(),
        carModel.getBrand(),
        carModel.getModel(),
        carModel.getSegment(),
        carModel.getPricePerDay(),
        carModel.getDepositAmount(),
        carModel.getMileageLimitPerDay(),
        carModel.getExtraMileageFee(),
        carModel.getPowerHp(),
        carModel.getTransmissionType(),
        carModel.getFuelType()
    );
    }
}
