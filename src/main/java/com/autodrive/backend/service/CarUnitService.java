package com.autodrive.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.autodrive.backend.dto.CarUnitResponse;
import com.autodrive.backend.repository.CarUnitRepository;

@Service
public class CarUnitService {
    private final CarUnitRepository carUnitRepository;

    public CarUnitService(CarUnitRepository carUnitRepository) {
        this.carUnitRepository = carUnitRepository;
    }

    public List<CarUnitResponse> getCarUnitsByModelId(Integer modelId) {
        return carUnitRepository.findByCarModel_Id(modelId)
        .stream()
        .map(carUnit -> new CarUnitResponse(
            carUnit.getId(),
            carUnit.getCarModel().getId(),
            carUnit.getVin(),
            carUnit.getLicensePlate(),
            carUnit.getColor(),
            carUnit.getStatus()
        )).toList();
    }

}
