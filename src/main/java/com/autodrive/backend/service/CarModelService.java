package com.autodrive.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;

import com.autodrive.backend.repository.CarModelRepository;

import com.autodrive.backend.dto.CarModelResponse;
import com.autodrive.backend.model.CarModel;

@Service
public class CarModelService {
    private final CarModelRepository carModelRepository;

    public CarModelService(CarModelRepository carModelRepository) {
        this.carModelRepository = carModelRepository;
    }

    public List<CarModelResponse> allCarModels(){
        return carModelRepository.findAll().stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    public CarModelResponse getCarModelById(Integer id){
        return carModelRepository.findById(id)
            .map(this::mapToDto)
            .orElse(null);
    }

    private CarModelResponse mapToDto(CarModel carModel) {
        CarModelResponse response = new CarModelResponse();
        response.setCar_model_id(carModel.getCar_model_id());
        response.setBrand(carModel.getBrand());
        response.setModel(carModel.getModel());
        response.setProductionYear(carModel.getProductionYear());
        response.setPricePerDay(carModel.getPricePerDay());
        return response;
    }
}
