package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.ExamSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamScheduleRepository extends JpaRepository<ExamSchedule, Long> {
    List<ExamSchedule> findByExaminationId(Long examinationId);
    Optional<ExamSchedule> findByExaminationIdAndSubjectId(Long examinationId, Long subjectId);
}
