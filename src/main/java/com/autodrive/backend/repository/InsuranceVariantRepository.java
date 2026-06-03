package com.autodrive.backend.repository;

import com.autodrive.backend.model.InsuranceVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InsuranceVariantRepository extends JpaRepository<InsuranceVariant, Integer> {
}