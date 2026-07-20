package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.AdmissionGuardian;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdmissionGuardianRepository extends JpaRepository<AdmissionGuardian, Long> {
    Optional<AdmissionGuardian> findByApplicationId(Long applicationId);
}
