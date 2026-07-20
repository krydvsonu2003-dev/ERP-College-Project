package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.Examination;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExaminationRepository extends JpaRepository<Examination, Long> {
    List<Examination> findByCourseIdAndSemester(Long courseId, Integer semester);
}
