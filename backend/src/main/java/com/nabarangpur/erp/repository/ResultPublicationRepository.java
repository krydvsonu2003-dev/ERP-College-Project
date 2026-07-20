package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.ResultPublication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResultPublicationRepository extends JpaRepository<ResultPublication, Long> {
    Optional<ResultPublication> findByExaminationId(Long examinationId);
}
