package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.StudentClassEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface StudentClassEnrollmentRepository extends JpaRepository<StudentClassEnrollment, Long> {

    @Query("""
        SELECT e
        FROM StudentClassEnrollment e
        WHERE e.classSection.id = :classSectionId
        AND e.semester = :semester
        AND e.student.deleted = false
    """)
    List<StudentClassEnrollment> findByClassSectionIdAndSemester(
            @Param("classSectionId") Long classSectionId,
            @Param("semester") Integer semester);

    List<StudentClassEnrollment> findByStudentId(Long studentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM StudentClassEnrollment e WHERE e.student.id = :studentId")
    void deleteByStudentId(@Param("studentId") Long studentId);
}