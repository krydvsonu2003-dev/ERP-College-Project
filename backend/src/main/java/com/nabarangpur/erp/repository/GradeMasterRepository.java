package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.GradeMaster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface GradeMasterRepository extends JpaRepository<GradeMaster, Long> {

    @Query("SELECT g FROM GradeMaster g WHERE :percentage >= g.minPercentage AND :percentage <= g.maxPercentage")
    Optional<GradeMaster> findByPercentage(@Param("percentage") BigDecimal percentage);
}
