package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.StudentGuardian;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentGuardianRepository extends JpaRepository<StudentGuardian, Long> {
    Optional<StudentGuardian> findByStudentId(Long studentId);
}
