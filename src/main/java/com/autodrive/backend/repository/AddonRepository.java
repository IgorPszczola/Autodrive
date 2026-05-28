package com.autodrive.backend.repository;

import com.autodrive.backend.model.Addon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddonRepository extends JpaRepository<Addon, Integer> {
    
}
