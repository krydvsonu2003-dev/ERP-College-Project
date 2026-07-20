package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.SemesterResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SemesterResultRepository extends JpaRepository<SemesterResult, Long> {
    Optional<SemesterResult> findByStudentIdAndAcademicYearIdAndSemester(Long studentId, Long academicYearId, Integer semester);
    List<SemesterResult> findByStudentIdOrderBySemesterAsc(Long studentId);
    
    @Transactional
    void deleteByStudentId(Long studentId);
}
