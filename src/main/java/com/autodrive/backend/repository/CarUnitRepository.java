package com.autodrive.backend.repository;
import com.autodrive.backend.model.CarUnit;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarUnitRepository extends JpaRepository<CarUnit, Integer> {

    List<CarUnit> findByCarModel_Id(Integer modelId);

    Optional<CarUnit> findByIdAndAvailable(Integer id, boolean available);
    
}
