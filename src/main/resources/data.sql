INSERT INTO roles (role_id, name) VALUES (1, 'ROLE_USER');
INSERT INTO roles (role_id, name) VALUES (2, 'ROLE_ADMIN');

INSERT INTO car_models (car_model_id, brand, model, segment, production_year, price_per_day, deposit_amount, mileage_limit_per_day, extra_mileage_fee, image_url, power_hp, transmission_type, fuel_type) 
VALUES (1, 'BMW', 'M3', 'G', 2023, 850.00, 2000.00, 200, 2.50, 'https://link-do-zdjecia.pl/m3.jpg', 510, 'AUTOMATIC', 'PETROL');

INSERT INTO car_models (car_model_id, brand, model, segment, production_year, price_per_day, deposit_amount, mileage_limit_per_day, extra_mileage_fee, image_url, power_hp, transmission_type, fuel_type) 
VALUES (2, 'Audi', 'RS6', 'G', 2024, 1200.00, 3000.00, 200, 3.50, 'https://link-do-zdjecia.pl/rs6.jpg', 600, 'AUTOMATIC', 'PETROL');

INSERT INTO car_models (car_model_id, brand, model, segment, production_year, price_per_day, deposit_amount, mileage_limit_per_day, extra_mileage_fee, image_url, power_hp, transmission_type, fuel_type) 
VALUES (3, 'Toyota', 'Yaris GR', 'C', 2022, 450.00, 1000.00, 100, 1.50, 'https://link-do-zdjecia.pl/yaris-gr.jpg', 165, 'MANUAL', 'PETROL');


INSERT INTO car_units (car_unit_id, car_model_id, license_plate, current_mileage, status, vin, color)
VALUES (1, 1, 'WI-777GA', 12000, 'AVAILABLE', 'VIN123456789BMW001', 'Black');

INSERT INTO car_units (car_unit_id, car_model_id, license_plate, current_mileage, status, vin, color)
VALUES (2, 1, 'PO-444X', 45000, 'MAINTENANCE', 'VIN123456789BMW002', 'White');

INSERT INTO car_units (car_unit_id, car_model_id, license_plate, current_mileage, status, vin, color)
VALUES (3, 2, 'WN-999RS', 8000, 'AVAILABLE', 'VIN987654321AUDI01', 'Nardo Grey');

INSERT INTO car_units (car_unit_id, car_model_id, license_plate, current_mileage, status, vin, color)
VALUES (4, 3, 'KR-111Y', 5000, 'AVAILABLE', 'VIN456789123TOYOTA01', 'Red');


INSERT INTO insurance_variants (insurance_id, name, price_per_day, description) VALUES (1, 'SILVER', 49.00, 'Podstawowe OC/AC, udział własny w szkodzie do 5000 PLN');
INSERT INTO insurance_variants (insurance_id, name, price_per_day, description) VALUES (2, 'GOLD', 99.00, 'Pełne AC, brak udziału własnego, ubezpieczenie szyb i opon');
INSERT INTO insurance_variants (insurance_id, name, price_per_day, description) VALUES (3, 'PLATINUM', 149.00, 'Wszystko to co GOLD + darmowy assistance 24/7 na terenie całej Europy i auto zastępcze');