package com.autodrive.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.autodrive.backend.dto.CarUnitResponse;
import com.autodrive.backend.dto.TerminResponse;
import com.autodrive.backend.model.CarModel;
import com.autodrive.backend.model.CarUnit;
import com.autodrive.backend.repository.CarModelRepository;
import com.autodrive.backend.repository.CarUnitRepository;
import com.autodrive.backend.repository.ReservationRepository;

import jakarta.transaction.Transactional;

@Service
public class CarUnitService {
    private final CarUnitRepository carUnitRepository;
    private final ReservationRepository reservationRepository;
    private final CarModelRepository carModelRepository;


    public CarUnitService(CarUnitRepository carUnitRepository, ReservationRepository reservationRepository, CarModelRepository carModelRepository) {
        this.carUnitRepository = carUnitRepository;
        this.reservationRepository = reservationRepository;
        this.carModelRepository = carModelRepository;
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
            carUnit.getProductionYear(),
            carUnit.getImageUrl(),
            carUnit.getStatus()
        )).toList();
    }

    public List<TerminResponse> getOccupiedDatesForCar(Long carUnitId) {
        return reservationRepository.findByCarUnitId(carUnitId)
            .stream()
            .map(reservation -> new TerminResponse(
                reservation.getStartDate(),
                reservation.getEndDate()
            ))
            .toList();
    }

    @Transactional
    public CarUnitResponse updateUnitStatus(Integer carUnitId, String newStatus) {
        CarUnit unit = carUnitRepository.findById(carUnitId)
                .orElseThrow(() -> new RuntimeException("Car unit not found"));


        List<String> allowedStatuses = List.of("AVAILABLE", "RENTED", "IN_REPAIR");
        if (!allowedStatuses.contains(newStatus.toUpperCase())) {
            throw new IllegalArgumentException("Invalid status. Allowed values are: " + allowedStatuses);
        }

        unit.setStatus(newStatus.toUpperCase());
        carUnitRepository.save(unit);

        return new CarUnitResponse(
            unit.getId(),
            unit.getCarModel().getId(),
            unit.getVin(),
            unit.getLicensePlate(),
            unit.getColor(),
            unit.getProductionYear(),
            unit.getImageUrl(),
            unit.getStatus()
        );
    }

    @Transactional
    public CarUnitResponse addCarUnit(com.autodrive.backend.dto.CarUnitRequest request) {

        CarModel model = carModelRepository.findById(request.carModelId())
                .orElseThrow(() -> new RuntimeException("Car model not found with id: " + request.carModelId()));

        CarUnit unit = new CarUnit();
        unit.setCarModel(model);
        unit.setLicensePlate(request.licensePlate());
        unit.setVin(request.vin());
        unit.setCurrentMileage(request.currentMileage());
        unit.setStatus("AVAILABLE");
        unit.setColor(request.color());
        unit.setProductionYear(request.productionYear());
        unit.setImageUrl(request.imageUrl());
    

        CarUnit savedUnit = carUnitRepository.save(unit);

        return new CarUnitResponse(
                savedUnit.getId(),
                savedUnit.getCarModel().getId(),
                savedUnit.getVin(),
                savedUnit.getLicensePlate(),
                savedUnit.getColor(),
                savedUnit.getProductionYear(),
                savedUnit.getImageUrl(),
                savedUnit.getStatus()
        );
    }
        
}
