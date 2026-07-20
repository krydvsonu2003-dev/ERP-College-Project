package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.AdmissionAcademic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdmissionAcademicRepository extends JpaRepository<AdmissionAcademic, Long> {
    Optional<AdmissionAcademic> findByApplicationId(Long applicationId);
}
