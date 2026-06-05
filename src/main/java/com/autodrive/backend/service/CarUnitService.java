package com.autodrive.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.autodrive.backend.dto.CarUnitResponse;
import com.autodrive.backend.dto.TerminResponse;
import com.autodrive.backend.model.CarUnit;
import com.autodrive.backend.repository.CarUnitRepository;
import com.autodrive.backend.repository.ReservationRepository;

import jakarta.transaction.Transactional;

@Service
public class CarUnitService {
    private final CarUnitRepository carUnitRepository;
    private final ReservationRepository reservationRepository;

    public CarUnitService(CarUnitRepository carUnitRepository, ReservationRepository reservationRepository) {
        this.carUnitRepository = carUnitRepository;
        this.reservationRepository = reservationRepository;
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
        
}
