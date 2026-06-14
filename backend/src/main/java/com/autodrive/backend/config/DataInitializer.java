package com.autodrive.backend.config;

import com.autodrive.backend.model.*;
import com.autodrive.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final CarModelRepository carModelRepository;
    private final CarUnitRepository carUnitRepository;
    private final InsuranceVariantRepository insuranceRepository;
    private final AddonRepository addonRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository,
                           CarModelRepository carModelRepository, CarUnitRepository carUnitRepository,
                           InsuranceVariantRepository insuranceRepository, AddonRepository addonRepository,
                           PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.carModelRepository = carModelRepository;
        this.carUnitRepository = carUnitRepository;
        this.insuranceRepository = insuranceRepository;
        this.addonRepository = addonRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        
        if (roleRepository.count() == 0) {
            Role userRole = new Role();
            userRole.setName("ROLE_USER");
            roleRepository.save(userRole);

            Role adminRole = new Role();
            adminRole.setName("ROLE_ADMIN");
            roleRepository.save(adminRole);
        }

        Role roleUser = roleRepository.findByName("ROLE_USER").orElse(null);
        Role roleAdmin = roleRepository.findByName("ROLE_ADMIN").orElse(null);

        if (userRepository.count() == 0) {
            User admin = new User();
            admin.setEmail("admin@autodrive.pl");
            admin.setPasswordHash(passwordEncoder.encode("Admin123!"));
            admin.setFirstName("Grzegorz");
            admin.setLastName("Brzęczyszczykiewicz");
            admin.setPhoneNumber("123456789");
            admin.setDriverLicenseNumber("XYZ98765");
            admin.setActive(true);
            if (roleAdmin != null) admin.setRoles(Set.of(roleAdmin));
            userRepository.save(admin);

            User user = new User();
            user.setEmail("user@autodrive.pl");
            user.setPasswordHash(passwordEncoder.encode("User123!"));
            user.setFirstName("Jan");
            user.setLastName("Kowalski");
            user.setPhoneNumber("987654321");
            user.setDriverLicenseNumber("ABC12345");
            user.setActive(true);
            if (roleUser != null) user.setRoles(Set.of(roleUser));
            userRepository.save(user);
        }

        if (insuranceRepository.count() == 0) {
            InsuranceVariant basic = new InsuranceVariant();
            basic.setName("Podstawowe OC/AC");
            basic.setDescription("Podstawowa ochrona z wkładem własnym do 2000 zł.");
            basic.setPricePerDay(new BigDecimal("0.00"));
            insuranceRepository.save(basic);

            InsuranceVariant full = new InsuranceVariant();
            full.setName("Pełne Premium (Szkoda 0)");
            full.setDescription("Pełne zniesienie udziału własnego w szkodzie. Jedziesz bez stresu.");
            full.setPricePerDay(new BigDecimal("59.00"));
            insuranceRepository.save(full);
        }

        if (addonRepository.count() == 0) {
            Addon gps = new Addon();
            gps.setName("Nawigacja GPS Premium");
            gps.setPricePerDay(new BigDecimal("15.00"));
            addonRepository.save(gps);

            Addon childSeat = new Addon();
            childSeat.setName("Fotelik dla dziecka (9-36kg)");
            childSeat.setPricePerDay(new BigDecimal("40.00"));
            addonRepository.save(childSeat);
        }

        if (carModelRepository.count() == 0) {
            
            CarModel teslaModel = new CarModel();
            teslaModel.setBrand("Tesla");
            teslaModel.setModel("Model 3");
            teslaModel.setSegment("D");
            teslaModel.setPricePerDay(new BigDecimal("250.00"));
            teslaModel.setDepositAmount(new BigDecimal("1000.00"));
            teslaModel.setExtraMileageFee(new BigDecimal("1.50"));
            teslaModel.setPowerHp(325);
            teslaModel.setTransmissionType("AUTOMATIC");
            teslaModel.setFuelType("ELECTRIC");
            teslaModel.setMinRentDays(1);
            carModelRepository.save(teslaModel);

            CarUnit t1 = new CarUnit(); 
            t1.setCarModel(teslaModel); 
            t1.setLicensePlate("EL-TES11"); 
            t1.setStatus("AVAILABLE"); 
            t1.setProductionYear(2023);
            t1.setVin("5YJ3E1EAXNF000001");
            t1.setColor("Czerwony");
            t1.setCurrentMileage(15000L);
            t1.setImageUrl("https://i.imgur.com/cCr3SVw.jpeg");
            carUnitRepository.save(t1);

            CarUnit t2 = new CarUnit(); 
            t2.setCarModel(teslaModel); 
            t2.setLicensePlate("EL-TES22"); 
            t2.setStatus("AVAILABLE"); 
            t2.setProductionYear(2024);
            t2.setVin("5YJ3E1EAXNF000002");
            t2.setColor("Czarny");
            t2.setCurrentMileage(5000L);
            carUnitRepository.save(t2);

            CarModel porscheModel = new CarModel();
            porscheModel.setBrand("Porsche");
            porscheModel.setModel("911 Carrera");
            porscheModel.setSegment("SPORT");
            porscheModel.setPricePerDay(new BigDecimal("800.00"));
            porscheModel.setDepositAmount(new BigDecimal("5000.00"));
            porscheModel.setExtraMileageFee(new BigDecimal("4.00"));
            porscheModel.setPowerHp(385);
            porscheModel.setTransmissionType("AUTOMATIC");
            porscheModel.setFuelType("PETROL");
            porscheModel.setMinRentDays(2);
            carModelRepository.save(porscheModel);

            CarUnit p1 = new CarUnit(); 
            p1.setCarModel(porscheModel); 
            p1.setLicensePlate("WI-911PC"); 
            p1.setStatus("AVAILABLE"); 
            p1.setProductionYear(2022);
            p1.setVin("WP0AA299XNS000001");
            p1.setColor("Czarny");
            p1.setCurrentMileage(28000L);
            p1.setImageUrl("https://i.imgur.com/q1iwSFn.png");
            carUnitRepository.save(p1);

            CarUnit p2 = new CarUnit(); 
            p2.setCarModel(porscheModel); 
            p2.setLicensePlate("WI-777PC"); 
            p2.setStatus("RENTED"); 
            p2.setProductionYear(2023);
            p2.setVin("WP0AA299XNS000002");
            p2.setColor("Szary");
            p2.setCurrentMileage(12000L);
            carUnitRepository.save(p2);

            CarModel bmwModel = new CarModel();
            bmwModel.setBrand("BMW");
            bmwModel.setModel("M340d xDrive");
            bmwModel.setSegment("D");
            bmwModel.setPricePerDay(new BigDecimal("490.00"));
            bmwModel.setDepositAmount(new BigDecimal("3500.00"));
            bmwModel.setExtraMileageFee(new BigDecimal("3.20"));
            bmwModel.setPowerHp(394);
            bmwModel.setTransmissionType("AUTOMATIC");
            bmwModel.setFuelType("DIESEL");
            bmwModel.setMinRentDays(2);
            carModelRepository.save(bmwModel);

            CarUnit b1 = new CarUnit(); 
            b1.setCarModel(bmwModel); 
            b1.setLicensePlate("PO-BMW01"); 
            b1.setStatus("AVAILABLE"); 
            b1.setProductionYear(2021);
            b1.setVin("WBA31AG0XNM000001");
            b1.setColor("Czarny");
            b1.setCurrentMileage(45000L);
            b1.setImageUrl("https://i.imgur.com/ZkK7Ez4.png");
            carUnitRepository.save(b1);

            CarUnit b2 = new CarUnit(); 
            b2.setCarModel(bmwModel); 
            b2.setLicensePlate("PO-BMW02"); 
            b2.setStatus("AVAILABLE"); 
            b2.setProductionYear(2023);
            b2.setVin("WBA31AG0XNM000002");
            b2.setColor("Czarny");
            b2.setCurrentMileage(18000L);
            carUnitRepository.save(b2);
        }


        System.out.println("[Autodrive DataInitializer] Baza danych została pomyślnie napełniona kompletem danych testowych");
    }
}