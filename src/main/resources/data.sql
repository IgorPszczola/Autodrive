INSERT INTO roles (role_id, name) VALUES (1, 'ROLE_USER');
INSERT INTO roles (role_id, name) VALUES (2, 'ROLE_ADMIN');

INSERT INTO car_models (car_model_id, brand, model, segment, production_year, price_per_day, deposit_amount, mileage_limit_per_day, extra_mileage_fee, image_url, power_hp, transmission_type, fuel_type) 
VALUES (1, 'BMW', 'M3', 'G', 2023, 850.00, 2000.00, 200, 2.50, 'https://link-do-zdjecia.pl/m3.jpg', 510, 'AUTOMATIC', 'PETROL');

-- Pełny insert dla Audi RS6
INSERT INTO car_models (car_model_id, brand, model, segment, production_year, price_per_day, deposit_amount, mileage_limit_per_day, extra_mileage_fee, image_url, power_hp, transmission_type, fuel_type) 
VALUES (2, 'Audi', 'RS6', 'G', 2024, 1200.00, 3000.00, 200, 3.50, 'https://link-do-zdjecia.pl/rs6.jpg', 600, 'AUTOMATIC', 'PETROL');

INSERT INTO car_models (car_model_id, brand, model, segment, production_year, price_per_day, deposit_amount, mileage_limit_per_day, extra_mileage_fee, image_url, power_hp, transmission_type, fuel_type) 
VALUES (3, 'Toyota', 'Yaris GR', 'C', 2022, 450.00, 1000.00, 100, 1.50, 'https://link-do-zdjecia.pl/yaris-gr.jpg', 165, 'MANUAL', 'PETROL');