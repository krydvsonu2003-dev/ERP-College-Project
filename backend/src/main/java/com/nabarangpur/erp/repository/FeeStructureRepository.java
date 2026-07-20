package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.FeeStructure;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeeStructureRepository extends JpaRepository<FeeStructure, Long> {
    List<FeeStructure> findByCourseIdAndAcademicYearIdAndSemester(Long courseId, Long academicYearId, Integer semester);
}
