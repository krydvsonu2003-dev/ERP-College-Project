package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.ClassSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassSectionRepository extends JpaRepository<ClassSection, Long> {
    List<ClassSection> findByCourseIdAndAcademicYearId(Long courseId, Long academicYearId);
    List<ClassSection> findByCourseIdAndSemester(Long courseId, Integer semester);
}
