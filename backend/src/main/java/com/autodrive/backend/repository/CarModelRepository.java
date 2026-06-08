package com.autodrive.backend.repository;
import com.autodrive.backend.model.CarModel;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CarModelRepository extends JpaRepository<CarModel, Integer>{
    
    @Query("SELECT cm FROM CarModel cm WHERE " +
           "(CAST(:brand AS string) IS NULL OR LOWER(cm.brand) LIKE LOWER(CONCAT('%', CAST(:brand AS string), '%'))) AND " +
           "(CAST(:fuelType AS string) IS NULL OR cm.fuelType = CAST(:fuelType AS string)) AND " +
           "(:maxPrice IS NULL OR cm.pricePerDay <= :maxPrice)")
    List<CarModel> findFiltered(
            @Param("brand") String brand,
            @Param("fuelType") String fuelType,
            @Param("maxPrice") BigDecimal maxPrice,
            Sort sort
    );
}
