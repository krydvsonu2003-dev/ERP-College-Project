package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.AdmissionApplication;
import com.nabarangpur.erp.entity.AdmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AdmissionApplicationRepository extends JpaRepository<AdmissionApplication, Long> {

    Optional<AdmissionApplication> findByAdmissionRefNo(String admissionRefNo);

    long countByStatus(AdmissionStatus status);

    @Query("SELECT a FROM AdmissionApplication a WHERE a.deleted = false")
    Page<AdmissionApplication> findByDeletedFalse(Pageable pageable);

    @Query("SELECT a FROM AdmissionApplication a WHERE a.status = :status AND a.deleted = false")
    Page<AdmissionApplication> findByStatusAndDeletedFalse(
            @Param("status") AdmissionStatus status,
            Pageable pageable);

    @Query("SELECT a FROM AdmissionApplication a WHERE a.id = :id AND a.deleted = false")
    Optional<AdmissionApplication> findByIdAndDeletedFalse(
            @Param("id") Long id);
}