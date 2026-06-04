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
import com.autodrive.backend.model.ReturnReport;
import com.autodrive.backend.model.User;
import com.autodrive.backend.repository.AddonRepository;
import com.autodrive.backend.repository.CarModelRepository;
import com.autodrive.backend.repository.CarUnitRepository;
import com.autodrive.backend.repository.InsuranceVariantRepository;
import com.autodrive.backend.repository.ReservationRepository;
import com.autodrive.backend.repository.ReturnReportRepository;
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
    private final ReturnReportRepository returnReportRepository;
    
    public ReservationService(ReservationRepository reservationRepository, UserRepository userRepository, CarUnitRepository carUnitRepository, AddonRepository addonRepository, InsuranceVariantRepository insuranceVariantRepository, CarModelRepository carModelRepository, ReturnReportRepository returnReportRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.carUnitRepository = carUnitRepository;
        this.addonRepository = addonRepository;
        this.insuranceVariantRepository = insuranceVariantRepository;
        this.carModelRepository = carModelRepository;
        this.returnReportRepository = returnReportRepository;
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
            throw new IllegalStateException("No available cars of this model for the selected dates!");
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

    @Transactional
    public void assignFirstAvailableCarUnit(Integer reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (!"CONFIRMED".equals(reservation.getStatus())) {
            throw new IllegalStateException("Only reservations with CONFIRMED status can be assigned a car unit");
        }

        Integer modelId = reservation.getCarModel().getId();

        List<CarUnit> availableUnits = carUnitRepository.findByCarModelIdAndStatus(modelId, "AVAILABLE");

        if (availableUnits.isEmpty()) {
            throw new IllegalStateException("No available car units for model " 
                    + reservation.getCarModel().getBrand() + " " + reservation.getCarModel().getModel() 
                    + " on parking lot. Propose customer to choose free upgrade.");
        }

        CarUnit assignedCar = availableUnits.get((int) (Math.random() * availableUnits.size()));

        reservation.setCarUnit(assignedCar);
        reservation.setStatus("ACTIVE"); 
        
        assignedCar.setStatus("RENTED");

        reservationRepository.save(reservation);
        carUnitRepository.save(assignedCar);
    }

    @Transactional
    public ReturnReport processReturn(Integer reservationId, Integer endMileage, BigDecimal damageCost, String damageDescription) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        if (!"ACTIVE".equals(reservation.getStatus())) {
            throw new IllegalStateException("Cannot return a car for a reservation that is not active!");
        }

        CarUnit carUnit = reservation.getCarUnit();
        CarModel carModel = reservation.getCarModel();

        Long startMileage = carUnit.getCurrentMileage();
        Long drivenDistance = endMileage - startMileage;

        long days = java.time.temporal.ChronoUnit.DAYS.between(reservation.getStartDate(), reservation.getEndDate());
        if (days == 0) days = 1;

        Long allowedMileage = carModel.getMileageLimitPerDay() * (long) days;
        BigDecimal extraMileageCost = BigDecimal.ZERO;

        if (drivenDistance > allowedMileage) {
            Long extraKm = drivenDistance - allowedMileage;
            extraMileageCost = BigDecimal.valueOf(extraKm).multiply(carModel.getExtraMileageFee());
        }

        BigDecimal totalSurcharge = extraMileageCost.add(damageCost);

        ReturnReport report = new ReturnReport();
        report.setReservation(reservation);
        report.setReturnDate(LocalDate.now());
        report.setEndMileage(endMileage);
        report.setExtraMileageCost(extraMileageCost.doubleValue());
        report.setDamageCost(damageCost.doubleValue());
        report.setTotalSurcharge(totalSurcharge.doubleValue());
        report.setDamageDescription(damageDescription);

        reservation.setStatus("COMPLETED");
        carUnit.setCurrentMileage(endMileage.longValue());
        
        if (damageCost.compareTo(BigDecimal.ZERO) > 0) {
            carUnit.setStatus("IN_REPAIR");
        } else {
            carUnit.setStatus("AVAILABLE");
        }

        reservationRepository.save(reservation);
        carUnitRepository.save(carUnit);
        return returnReportRepository.save(report);
    }

    public List<Reservation> getMyReservations(String email) {
        return reservationRepository.findByUserEmailOrderByCreatedAtDesc(email);
    }

    @Transactional
    public void cancelReservation(Integer reservationId, String email) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new RuntimeException("Reservation not found"));

        
        if (!reservation.getUser().getEmail().equals(email)) {
            throw new IllegalStateException("You do not have permission to cancel this reservation!");
        }


        if (!"PENDING".equals(reservation.getStatus()) && !"CONFIRMED".equals(reservation.getStatus())) {
            throw new IllegalStateException("Cannot cancel a reservation with status: " + reservation.getStatus());
        }

    
        if (reservation.getStartDate().isBefore(java.time.LocalDate.now())) {
            throw new IllegalStateException("Cannot cancel a reservation that has already started!");
        }

    
        reservation.setStatus("CANCELLED");

    
        if (reservation.getCarUnit() != null) {
            com.autodrive.backend.model.CarUnit unit = reservation.getCarUnit();
            unit.setStatus("AVAILABLE");
            carUnitRepository.save(unit);
            reservation.setCarUnit(null);
        }

        reservationRepository.save(reservation);
    }

}
