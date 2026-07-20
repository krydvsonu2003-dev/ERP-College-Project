package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.AdmissionDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdmissionDocumentRepository extends JpaRepository<AdmissionDocument, Long> {
    List<AdmissionDocument> findByApplicationId(Long applicationId);
}
