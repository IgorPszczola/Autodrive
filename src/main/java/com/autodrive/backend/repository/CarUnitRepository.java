package com.autodrive.backend.repository;
import com.autodrive.backend.model.CarUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarUnitRepository extends JpaRepository<CarUnit, Integer> {
    
}
