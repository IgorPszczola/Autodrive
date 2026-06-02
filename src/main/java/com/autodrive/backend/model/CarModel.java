package com.autodrive.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;


@Entity
@Table(name = "car_models")
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CarModel {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "car_model_id")
	private Integer id;

	@Column(nullable = false)
	private String brand;

	@Column(name = "model", nullable = false, length = 50)
	private String model;

	@Column(nullable = false, length = 10)
	private String segment;

	@Column(name = "production_year", nullable = false)
	private Integer productionYear;
	@Column(name = "price_per_day", nullable = false, precision = 10, scale = 2)
	private BigDecimal pricePerDay;

	@Column(name = "deposit_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal depositAmount;

	@Column(name = "mileage_limit_per_day", nullable = false, columnDefinition = "INT DEFAULT 200")
	private Integer mileageLimitPerDay = 200;

	@Column(name = "extra_mileage_fee", nullable = false, precision = 10, scale = 2)
	private BigDecimal extraMileageFee;

	@Column(name = "image_url", length = 255)
	private String imageUrl;

	@Column(name = "power_hp", nullable = false)
	private Integer powerHp;

	@Column(name = "transmission_type", nullable = false, length = 20)
	private String transmissionType;

	@Column(name = "fuel_type", length = 20)
	private String fuelType;
}
