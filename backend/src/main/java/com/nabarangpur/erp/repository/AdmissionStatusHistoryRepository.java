package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.AdmissionStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdmissionStatusHistoryRepository extends JpaRepository<AdmissionStatusHistory, Long> {
    List<AdmissionStatusHistory> findByApplicationIdOrderByChangedAtAsc(Long applicationId);
}
