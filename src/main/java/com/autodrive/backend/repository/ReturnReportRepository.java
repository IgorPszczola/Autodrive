package com.autodrive.backend.repository;

import com.autodrive.backend.model.ReturnReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReturnReportRepository extends JpaRepository<ReturnReport, Integer> {
    
}
