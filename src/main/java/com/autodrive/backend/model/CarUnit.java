package com.autodrive.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "car_units")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CarUnit {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "car_unit_id")
	private Long id;

	@Column(name = "license_plate", nullable = false, unique = true)
	private String licensePlate;

	@Column(name = "current_mileage")
	private Long currentMileage;

	@Column(name = "status")
	private String status;

	@Column(name = "vin", nullable = false, unique = true)
	private String vin;

	@Column(name = "color")
	private String color;	

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "car_model_id")
	private CarModel carModel;

}
