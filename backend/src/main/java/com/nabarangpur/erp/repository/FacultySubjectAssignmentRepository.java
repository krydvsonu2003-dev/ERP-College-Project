package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.FacultySubjectAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FacultySubjectAssignmentRepository extends JpaRepository<FacultySubjectAssignment, Long> {
    List<FacultySubjectAssignment> findByFacultyId(Long facultyId);

    boolean existsByFacultyIdAndSubjectIdAndClassSectionId(Long facultyId, Long subjectId, Long classSectionId);
}
