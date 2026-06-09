package com.autodrive.backend.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Service;

import com.autodrive.backend.dto.AddonResponse;
import com.autodrive.backend.dto.CarRequestReturn;
import com.autodrive.backend.dto.CarReturnResponse;
import com.autodrive.backend.dto.DashboardStatsResponse;
import com.autodrive.backend.dto.ReservationRequest;
import com.autodrive.backend.dto.ReservationResponse;
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

        long totalFleetCount = carUnitRepository.countByCarModelIdAndStatus(carModelId, "AVAILABLE");
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
        BigDecimal currentTotalPrice = baseVehiclePrice;

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

        long completedReservationsCount = reservationRepository.countByUserIdAndStatusNot(user.getId(), "CANCELLED");
        long currentOrderNumber = completedReservationsCount + 1;
        double discount = 0.0;
        
        if (currentOrderNumber % 5 == 0) {
            discount = 10.0;
            currentTotalPrice = currentTotalPrice.multiply(BigDecimal.valueOf(0.9));
        }

        reservation.setDiscountApplied(discount);
        reservation.setTotalPrice(currentTotalPrice);

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
        reservation.setStatus("RENTED");
        assignedCar.setStatus("RENTED");

        reservationRepository.save(reservation);
        carUnitRepository.save(assignedCar);
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


    public DashboardStatsResponse getDashboardStats() {

        List<Reservation> validReservations = reservationRepository.findAll().stream()
                .filter(r -> !r.getStatus().equals("CANCELLED"))
                .toList();

        BigDecimal totalEarnings = validReservations.stream()
                .map(Reservation::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalReservationsCount = validReservations.size();

        long totalFleetCount = carUnitRepository.count();
        long activeRentalsCount = carUnitRepository.countByStatus("RENTED");

        long fleetInRepairCount = carUnitRepository.countByStatus("IN_REPAIR") + carUnitRepository.countByStatus("MAINTENANCE");

        double fleetUtilizationRate = 0.0;
        if (totalFleetCount > 0) {
            fleetUtilizationRate = ((double) activeRentalsCount / totalFleetCount) * 100.0;
        }

        return new DashboardStatsResponse(
                totalEarnings,
                totalReservationsCount,
                activeRentalsCount,
                fleetInRepairCount,
                Math.round(fleetUtilizationRate * 100.0) / 100.0
        );
    }

    @Transactional
    public CarReturnResponse processCarReturn(Integer reservationId, CarRequestReturn request) {

        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new RuntimeException("Reservation not found"));
        
        if (!reservation.getStatus().equals("RENTED")){
            throw new IllegalStateException("Only reservations with RENTED status can be processed for return");
        }    
        
        CarUnit carUnit = reservation.getCarUnit();
        if (carUnit == null) {
            throw new IllegalStateException("This reservation does not have a car assigned!");
        }


        if (request.currentMileage() < carUnit.getCurrentMileage()) {
            throw new IllegalArgumentException("Current mileage cannot be lower than the starting mileage (" + carUnit.getCurrentMileage() + " km)!");
        }

        long distanceTraveled = request.currentMileage() - carUnit.getCurrentMileage();
        carUnit.setCurrentMileage(request.currentMileage());

        long rentalDays = ChronoUnit.DAYS.between(reservation.getStartDate(), reservation.getEndDate());
        if (rentalDays == 0) rentalDays = 1;

        
        Long allowedMileage = reservation.getCarModel().getMileageLimitPerDay() * rentalDays;
        BigDecimal extraFee = BigDecimal.ZERO;

        if (distanceTraveled > allowedMileage) {
            long exceededKm = distanceTraveled - allowedMileage;
            BigDecimal feePerKm = reservation.getCarModel().getExtraMileageFee();
            extraFee = BigDecimal.valueOf(exceededKm).multiply(feePerKm);
            
            reservation.setTotalPrice(reservation.getTotalPrice().add(extraFee));
        }

        ReturnReport returnReport = new ReturnReport();
        returnReport.setReservation(reservation);
        returnReport.setReturnDate(LocalDate.now());
        returnReport.setEndMileage(request.currentMileage());
        returnReport.setExtraMileageCost(extraFee.doubleValue());

        double damageCostValue = 0.0;
        if (request.isDamaged()) {
            carUnit.setStatus("IN_REPAIR");
            damageCostValue = request.damageCost() != null ? request.damageCost().doubleValue() : 0.0;
            returnReport.setDamageDescription(request.damageNotes() != null ? request.damageNotes() : "No damage description provided");
            returnReport.setDamageCost(damageCostValue);
        } else {
            carUnit.setStatus("AVAILABLE");
            returnReport.setDamageCost(0.0);
        }

        double totalSurchargeValue = extraFee.doubleValue() + damageCostValue;
        returnReport.setTotalSurcharge(totalSurchargeValue);

        reservation.setStatus("COMPLETED");

        returnReportRepository.save(returnReport);
        Reservation savedReservation = reservationRepository.save(reservation);

        return new CarReturnResponse(
            savedReservation.getId(),
            savedReservation.getStatus(),
            returnReport.getReturnDate(),
            returnReport.getEndMileage(),
            returnReport.getExtraMileageCost(),
            (double) distanceTraveled,
            returnReport.getDamageCost(),
            returnReport.getDamageDescription(),
            savedReservation.getTotalPrice()
        );
    
    }

    public ReturnReport getReturnReportByReservationId(Integer reservationId) {
        return returnReportRepository.findByReservationId(reservationId)
            .orElseThrow(() -> new RuntimeException("Return report not found for reservation id: " + reservationId));
    }


  public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
            .map(reservation -> new ReservationResponse(
                reservation.getId(),
                reservation.getStartDate(),
                reservation.getEndDate(),
                reservation.getBasePrice(),
                reservation.getDiscountApplied(),
                reservation.getTotalPrice(),
                reservation.getCarModel() != null ? reservation.getCarModel().getDepositAmount() : BigDecimal.ZERO,
                reservation.getStatus(),
                reservation.getCreatedAt(),
                reservation.getUser().getEmail(),
                reservation.getCarModel().getBrand(),
                reservation.getCarModel().getModel(),
                reservation.getCarUnit() != null ? reservation.getCarUnit().getLicensePlate() : null,
                reservation.getInsuranceVariant() != null ? reservation.getInsuranceVariant().getName() : null,
                reservation.getAddons().stream().map(addon -> new AddonResponse(
                    addon.getId(),
                    addon.getName(),
                    addon.getDescription(),
                    addon.getPricePerDay()
                )).toList()
            ))
            .toList();
    }

}
