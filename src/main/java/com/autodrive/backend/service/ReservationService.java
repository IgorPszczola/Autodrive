package com.autodrive.backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.autodrive.backend.dto.ReservationRequest;
import com.autodrive.backend.model.Addon;
import com.autodrive.backend.model.CarModel;
import com.autodrive.backend.model.CarUnit;
import com.autodrive.backend.model.InsuranceVariant;
import com.autodrive.backend.model.Reservation;
import com.autodrive.backend.model.User;
import com.autodrive.backend.repository.AddonRepository;
import com.autodrive.backend.repository.CarModelRepository;
import com.autodrive.backend.repository.CarUnitRepository;
import com.autodrive.backend.repository.InsuranceVariantRepository;
import com.autodrive.backend.repository.ReservationRepository;
import com.autodrive.backend.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final CarUnitRepository carUnitRepository;
    private final AddonRepository addonRepository;
    private final InsuranceVariantRepository insuranceVariantRepository;
    private final CarModelRepository carModelRepository;
    
    public ReservationService(ReservationRepository reservationRepository, UserRepository userRepository, CarUnitRepository carUnitRepository, AddonRepository addonRepository, InsuranceVariantRepository insuranceVariantRepository, CarModelRepository carModelRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.carUnitRepository = carUnitRepository;
        this.addonRepository = addonRepository;
        this.insuranceVariantRepository = insuranceVariantRepository;
        this.carModelRepository = carModelRepository;
    }


    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    

    @Transactional
public Reservation makeReservation(Integer carModelId, String email, ReservationRequest request) {

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

    CarModel carModel = carModelRepository.findById(carModelId)
            .orElseThrow(() -> new RuntimeException("Car model not found"));

    LocalDate requestedStartDate = request.startDate().toLocalDate();
    LocalDate requestedEndDate = request.endDate().toLocalDate();
    
    if (requestedStartDate.isAfter(requestedEndDate)) {
        throw new IllegalArgumentException("Start date cannot be after end date");
    }

    long totalFleetCount = carUnitRepository.countByCarModelIdAndStatusNot(carModelId, "IN_REPAIR");
    long activeReservationsCount = reservationRepository.countOverlappingReservations(
            carModelId, requestedStartDate, requestedEndDate
    );

    if (activeReservationsCount >= totalFleetCount) {
        throw new IllegalStateException("Brak dostępnych samochodów tego modelu w wybranym terminie!");
    }

    long days = ChronoUnit.DAYS.between(requestedStartDate, requestedEndDate);
    if (days == 0) {
        days = 1;
    }

    BigDecimal baseVehiclePrice = carModel.getPricePerDay().multiply(BigDecimal.valueOf(days));
    BigDecimal currentTotalPrice = baseVehiclePrice.add(carModel.getDepositAmount());

    Reservation reservation = new Reservation();
    reservation.setUser(user);
    reservation.setCarModel(carModel);
    reservation.setCarUnit(null);
    reservation.setStartDate(requestedStartDate);
    reservation.setEndDate(requestedEndDate);
    reservation.setStatus("CONFIRMED");

    if (request.insuranceVariantId() == null) {
        throw new IllegalArgumentException("You have to choose an insurance variant");
    }

    InsuranceVariant insurance = insuranceVariantRepository.findById(request.insuranceVariantId())
            .orElseThrow(() -> new RuntimeException("Insurance variant not found"));

    reservation.setInsuranceVariant(insurance);
    
    BigDecimal insuranceCost = insurance.getPricePerDay().multiply(BigDecimal.valueOf(days));
    currentTotalPrice = currentTotalPrice.add(insuranceCost);

    List<Addon> addons = addonRepository.findAllById(request.addonIds());
    for (Addon addon : addons) {
        reservation.getAddons().add(addon);
        BigDecimal addonCost = addon.getPricePerDay().multiply(BigDecimal.valueOf(days));
        currentTotalPrice = currentTotalPrice.add(addonCost);
    }

    reservation.setBasePrice(baseVehiclePrice);

    int currentOrderNumber = user.getNumOfReservations() + 1;
    double discount = 0.0;
    
    if (currentOrderNumber % 5 == 0) {
        discount = 10.0;
        currentTotalPrice = currentTotalPrice.multiply(BigDecimal.valueOf(0.9));
    }

    reservation.setDiscountApplied(discount);
    reservation.setTotalPrice(currentTotalPrice);
    
    user.setNumOfReservations(currentOrderNumber);

    return reservationRepository.save(reservation);
}

}
