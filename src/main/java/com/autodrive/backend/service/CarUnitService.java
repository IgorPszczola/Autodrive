package com.autodrive.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.autodrive.backend.dto.CarUnitResponse;
import com.autodrive.backend.dto.TerminResponse;
import com.autodrive.backend.repository.CarUnitRepository;
import com.autodrive.backend.repository.ReservationRepository;

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

}
