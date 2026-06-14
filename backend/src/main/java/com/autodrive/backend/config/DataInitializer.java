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
            teslaModel.setPricePerDay(new BigDecimal("490.00"));
            teslaModel.setDepositAmount(new BigDecimal("2500.00"));
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
            porscheModel.setPricePerDay(new BigDecimal("2500.00"));
            porscheModel.setDepositAmount(new BigDecimal("10000.00"));
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
            bmwModel.setPricePerDay(new BigDecimal("790.00"));
            bmwModel.setDepositAmount(new BigDecimal("5000.00"));
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

            // 4. Audi RS6 Avant
            CarModel audiModel = new CarModel();
            audiModel.setBrand("Audi");
            audiModel.setModel("RS6 Avant");
            audiModel.setSegment("SPORT");
            audiModel.setPricePerDay(new BigDecimal("1800.00"));
            audiModel.setDepositAmount(new BigDecimal("8000.00"));
            audiModel.setExtraMileageFee(new BigDecimal("3.50"));
            audiModel.setPowerHp(600);
            audiModel.setTransmissionType("AUTOMATIC");
            audiModel.setFuelType("PETROL");
            audiModel.setMinRentDays(2);
            carModelRepository.save(audiModel);

            CarUnit au1 = new CarUnit();
            au1.setCarModel(audiModel);
            au1.setLicensePlate("KR-RS601");
            au1.setStatus("AVAILABLE");
            au1.setProductionYear(2022);
            au1.setVin("WAUZZZ4G0XN000001");
            au1.setColor("Szary mat");
            au1.setCurrentMileage(32000L);
            au1.setImageUrl("https://i.imgur.com/ERs1Jyl.png");
            carUnitRepository.save(au1);

            // 5. Mercedes-Benz C200
            CarModel mercModel = new CarModel();
            mercModel.setBrand("Mercedes-Benz");
            mercModel.setModel("C200");
            mercModel.setSegment("D");
            mercModel.setPricePerDay(new BigDecimal("390.00"));
            mercModel.setDepositAmount(new BigDecimal("2000.00"));
            mercModel.setExtraMileageFee(new BigDecimal("1.80"));
            mercModel.setPowerHp(204);
            mercModel.setTransmissionType("AUTOMATIC");
            mercModel.setFuelType("PETROL");
            mercModel.setMinRentDays(1);
            carModelRepository.save(mercModel);

            CarUnit me1 = new CarUnit();
            me1.setCarModel(mercModel);
            me1.setLicensePlate("WI-MERC1");
            me1.setStatus("AVAILABLE");
            me1.setProductionYear(2023);
            me1.setVin("WDD2050401N000001");
            me1.setColor("Srebrny");
            me1.setCurrentMileage(15000L);
            me1.setImageUrl("https://i.imgur.com/fWDiKQp.png");
            carUnitRepository.save(me1);

            // 6. Volkswagen Golf GTI
            CarModel golfModel = new CarModel();
            golfModel.setBrand("Volkswagen");
            golfModel.setModel("Golf GTI");
            golfModel.setSegment("C");
            golfModel.setPricePerDay(new BigDecimal("290.00"));
            golfModel.setDepositAmount(new BigDecimal("1500.00"));
            golfModel.setExtraMileageFee(new BigDecimal("1.40"));
            golfModel.setPowerHp(245);
            golfModel.setTransmissionType("AUTOMATIC");
            golfModel.setFuelType("PETROL");
            golfModel.setMinRentDays(1);
            carModelRepository.save(golfModel);

            CarUnit vw1 = new CarUnit();
            vw1.setCarModel(golfModel);
            vw1.setLicensePlate("GD-GOLF1");
            vw1.setStatus("AVAILABLE");
            vw1.setProductionYear(2022);
            vw1.setVin("WVWZZZCDZNW000001");
            vw1.setColor("Czerwony");
            vw1.setCurrentMileage(24000L);
            vw1.setImageUrl("https://i.imgur.com/YwBS3YN.jpeg");
            carUnitRepository.save(vw1);

            // 7. Ford Mustang GT
            CarModel mustangModel = new CarModel();
            mustangModel.setBrand("Ford");
            mustangModel.setModel("Mustang GT");
            mustangModel.setSegment("SPORT");
            mustangModel.setPricePerDay(new BigDecimal("890.00"));
            mustangModel.setDepositAmount(new BigDecimal("4000.00"));
            mustangModel.setExtraMileageFee(new BigDecimal("3.00"));
            mustangModel.setPowerHp(450);
            mustangModel.setTransmissionType("MANUAL");
            mustangModel.setFuelType("PETROL");
            mustangModel.setMinRentDays(2);
            carModelRepository.save(mustangModel);

            CarUnit fo1 = new CarUnit();
            fo1.setCarModel(mustangModel);
            fo1.setLicensePlate("WI-MUST1");
            fo1.setStatus("AVAILABLE");
            fo1.setProductionYear(2021);
            fo1.setVin("1FA6P8CF0MN000001");
            fo1.setColor("Żółty");
            fo1.setCurrentMileage(35000L);
            fo1.setImageUrl("https://i.imgur.com/UczTUkj.jpeg");
            carUnitRepository.save(fo1);

            // 8. Hyundai i30 N
            CarModel hyundaiModel = new CarModel();
            hyundaiModel.setBrand("Hyundai");
            hyundaiModel.setModel("i30 N Performance");
            hyundaiModel.setSegment("C");
            hyundaiModel.setPricePerDay(new BigDecimal("320.00"));
            hyundaiModel.setDepositAmount(new BigDecimal("1500.00"));
            hyundaiModel.setExtraMileageFee(new BigDecimal("1.50"));
            hyundaiModel.setPowerHp(280);
            hyundaiModel.setTransmissionType("MANUAL");
            hyundaiModel.setFuelType("PETROL");
            hyundaiModel.setMinRentDays(1);
            carModelRepository.save(hyundaiModel);

            CarUnit hy1 = new CarUnit();
            hy1.setCarModel(hyundaiModel);
            hy1.setLicensePlate("PO-I30N1");
            hy1.setStatus("AVAILABLE");
            hy1.setProductionYear(2022);
            hy1.setVin("TMAH351AANW000001");
            hy1.setColor("Niebieski");
            hy1.setCurrentMileage(21000L);
            hy1.setImageUrl("https://i.imgur.com/PRrHbWn.png");
            carUnitRepository.save(hy1);

            // 9. Toyota RAV4
            CarModel rav4Model = new CarModel();
            rav4Model.setBrand("Toyota");
            rav4Model.setModel("RAV4 Hybrid");
            rav4Model.setSegment("SUV");
            rav4Model.setPricePerDay(new BigDecimal("350.00"));
            rav4Model.setDepositAmount(new BigDecimal("1500.00"));
            rav4Model.setExtraMileageFee(new BigDecimal("1.20"));
            rav4Model.setPowerHp(222);
            rav4Model.setTransmissionType("AUTOMATIC");
            rav4Model.setFuelType("HYBRID");
            rav4Model.setMinRentDays(1);
            carModelRepository.save(rav4Model);

            CarUnit to1 = new CarUnit();
            to1.setCarModel(rav4Model);
            to1.setLicensePlate("KR-RAV41");
            to1.setStatus("AVAILABLE");
            to1.setProductionYear(2023);
            to1.setVin("JTMDFRFV0ND000001");
            to1.setColor("Biały");
            to1.setCurrentMileage(19000L);
            to1.setImageUrl("https://i.imgur.com/gQ71kyR.png");
            carUnitRepository.save(to1);

            // 10. Volvo XC90
            CarModel volvoModel = new CarModel();
            volvoModel.setBrand("Volvo");
            volvoModel.setModel("XC90 B5");
            volvoModel.setSegment("SUV");
            volvoModel.setPricePerDay(new BigDecimal("990.00"));
            volvoModel.setDepositAmount(new BigDecimal("4000.00"));
            volvoModel.setExtraMileageFee(new BigDecimal("2.20"));
            volvoModel.setPowerHp(235);
            volvoModel.setTransmissionType("AUTOMATIC");
            volvoModel.setFuelType("DIESEL");
            volvoModel.setMinRentDays(1);
            carModelRepository.save(volvoModel);

            CarUnit vo2 = new CarUnit();
            vo2.setCarModel(volvoModel);
            vo2.setLicensePlate("GD-XC901");
            vo2.setStatus("AVAILABLE");
            vo2.setProductionYear(2022);
            vo2.setVin("YV4LF70Y0MN000001");
            vo2.setColor("Granatowy");
            vo2.setCurrentMileage(41000L);
            vo2.setImageUrl("https://i.imgur.com/41gJnTN.jpeg");
            carUnitRepository.save(vo2);

            // 11. Fiat 500e
            CarModel fiatModel = new CarModel();
            fiatModel.setBrand("Fiat");
            fiatModel.setModel("500e");
            fiatModel.setSegment("A");
            fiatModel.setPricePerDay(new BigDecimal("190.00"));
            fiatModel.setDepositAmount(new BigDecimal("1000.00"));
            fiatModel.setExtraMileageFee(new BigDecimal("1.00"));
            fiatModel.setPowerHp(118);
            fiatModel.setTransmissionType("AUTOMATIC");
            fiatModel.setFuelType("ELECTRIC");
            fiatModel.setMinRentDays(1);
            carModelRepository.save(fiatModel);

            CarUnit fi1 = new CarUnit();
            fi1.setCarModel(fiatModel);
            fi1.setLicensePlate("WA-500EE");
            fi1.setStatus("AVAILABLE");
            fi1.setProductionYear(2023);
            fi1.setVin("ZFA3320000N000001");
            fi1.setColor("Zielony miętowy");
            fi1.setCurrentMileage(8000L);
            fi1.setImageUrl("https://i.imgur.com/BptBmar.jpeg");
            carUnitRepository.save(fi1);

            // 12. Lexus RX500h
            CarModel lexusModel = new CarModel();
            lexusModel.setBrand("Lexus");
            lexusModel.setModel("RX 500h F Sport");
            lexusModel.setSegment("SUV");
            lexusModel.setPricePerDay(new BigDecimal("1100.00"));
            lexusModel.setDepositAmount(new BigDecimal("5000.00"));
            lexusModel.setExtraMileageFee(new BigDecimal("2.80"));
            lexusModel.setPowerHp(371);
            lexusModel.setTransmissionType("AUTOMATIC");
            lexusModel.setFuelType("HYBRID");
            lexusModel.setMinRentDays(2);
            carModelRepository.save(lexusModel);

            CarUnit le1 = new CarUnit();
            le1.setCarModel(lexusModel);
            le1.setLicensePlate("PO-LEX01");
            le1.setStatus("AVAILABLE");
            le1.setProductionYear(2023);
            le1.setVin("JTJBJRBZ0ND000001");
            le1.setColor("Grafitowy");
            le1.setCurrentMileage(11000L);
            le1.setImageUrl("https://i.imgur.com/TI6eRMD.png");
            carUnitRepository.save(le1);

            // 13. Mazda MX-5
            CarModel mazdaModel = new CarModel();
            mazdaModel.setBrand("Mazda");
            mazdaModel.setModel("MX-5 Roadster");
            mazdaModel.setSegment("SPORT");
            mazdaModel.setPricePerDay(new BigDecimal("290.00"));
            mazdaModel.setDepositAmount(new BigDecimal("1500.00"));
            mazdaModel.setExtraMileageFee(new BigDecimal("1.50"));
            mazdaModel.setPowerHp(184);
            mazdaModel.setTransmissionType("MANUAL");
            mazdaModel.setFuelType("PETROL");
            mazdaModel.setMinRentDays(1);
            carModelRepository.save(mazdaModel);

            CarUnit ma1 = new CarUnit();
            ma1.setCarModel(mazdaModel);
            ma1.setLicensePlate("GD-MX501");
            ma1.setStatus("AVAILABLE");
            ma1.setProductionYear(2022);
            ma1.setVin("JM1ND2970N0000001");
            ma1.setColor("Czerwony Soul Red");
            ma1.setCurrentMileage(18000L);
            ma1.setImageUrl("https://i.imgur.com/mhKhs72.jpeg");
            carUnitRepository.save(ma1);
        }


        System.out.println("[Autodrive DataInitializer] Baza danych została pomyślnie napełniona kompletem danych testowych");
    }
}