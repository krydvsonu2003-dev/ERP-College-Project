package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.StudentAcademic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentAcademicRepository extends JpaRepository<StudentAcademic, Long> {
    Optional<StudentAcademic> findByStudentId(Long studentId);
}
