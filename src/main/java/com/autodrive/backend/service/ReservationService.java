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
import com.autodrive.backend.model.CarUnit;
import com.autodrive.backend.model.Reservation;
import com.autodrive.backend.model.User;
import com.autodrive.backend.repository.AddonRepository;
import com.autodrive.backend.repository.CarUnitRepository;
import com.autodrive.backend.repository.ReservationRepository;
import com.autodrive.backend.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final CarUnitRepository carUnitRepository;
    private final AddonRepository addonRepository;
    
    public ReservationService(ReservationRepository reservationRepository, UserRepository userRepository, CarUnitRepository carUnitRepository, AddonRepository addonRepository) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.carUnitRepository = carUnitRepository;
        this.addonRepository = addonRepository;
    }


    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
    }
    

    @Transactional
    public Reservation makeReservation(Integer carUnitID, String email, ReservationRequest request){
        
        if (request.startDate().isAfter(request.endDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        LocalDate requestedStartDate = request.startDate().toLocalDate();
        LocalDate requestedEndDate = request.endDate().toLocalDate();

        boolean isOccupied = reservationRepository.existsCollision(carUnitID, requestedStartDate, requestedEndDate);
        if (isOccupied) {
            throw new IllegalStateException("Ten samochód jest już zarezerwowany w wybranym terminie!");
        }

        User user = getUserByEmail(email);
        
        CarUnit car = carUnitRepository.findById(carUnitID)
            .orElseThrow(() -> new RuntimeException("Car unit not found"));

        if ("MAINTENANCE".equals(car.getStatus())) {
            throw new IllegalStateException("Samochód jest obecnie wyłączony z użytku (serwis).");
        }

        long days = ChronoUnit.DAYS.between(request.startDate(), request.endDate());
        if (days == 0) {
            days = 1;
        }
        BigDecimal price = car.getCarModel().getPricePerDay().multiply(BigDecimal.valueOf(days));

        price = price.add(car.getCarModel().getDepositAmount());

        Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setCarUnit(car);
        reservation.setStartDate(request.startDate().toLocalDate());
        reservation.setEndDate(request.endDate().toLocalDate());
        reservation.setBasePrice(price);
        reservation.setCarModel(car.getCarModel());

        reservation.setStatus("CONFIRMED");

        List<Addon> addons = addonRepository.findAllById(request.addonIds());
                
        for (Addon addon : addons) {
            reservation.getAddons().add(addon);
            BigDecimal addonCost = addon.getPricePerDay().multiply(BigDecimal.valueOf(days));
            reservation.setBasePrice(reservation.getBasePrice().add(addonCost));
            price = price.add(addonCost);
        }

        int currentOrderNumber = user.getNumOfReservations() + 1;

        double discount = 0.0;
        if (currentOrderNumber % 5 == 0){
            discount = 10.0;
            price = price.multiply(BigDecimal.valueOf(0.9));
        }

        reservation.setDiscountApplied(discount);
        reservation.setTotalPrice(price);

        user.setNumOfReservations(currentOrderNumber);

        reservationRepository.save(reservation);
        return reservation;
    }

}
