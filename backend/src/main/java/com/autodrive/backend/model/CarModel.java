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

	@Column(name = "brand", nullable = false, columnDefinition = "varchar(100)")
	private String brand;

	@Column(name = "model", nullable = false, columnDefinition = "varchar(50)")
	private String model;

	@Column(name = "segment", nullable = false, columnDefinition = "varchar(10)")
	private String segment;

	@Column(name = "price_per_day", nullable = false, precision = 10, scale = 2)
	private BigDecimal pricePerDay;

	@Column(name = "deposit_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal depositAmount;

	@Column(name = "mileage_limit_per_day", nullable = false, columnDefinition = "INT DEFAULT 200")
	private Integer mileageLimitPerDay = 200;

	@Column(name = "extra_mileage_fee", nullable = false, precision = 10, scale = 2)
	private BigDecimal extraMileageFee;

	@Column(name = "power_hp", nullable = false)
	private Integer powerHp;

	@Column(name = "transmission_type", nullable = false, columnDefinition = "varchar(20)")
	private String transmissionType;

	@Column(name = "fuel_type", nullable = false, columnDefinition = "varchar(50)")
	private String fuelType;
}
