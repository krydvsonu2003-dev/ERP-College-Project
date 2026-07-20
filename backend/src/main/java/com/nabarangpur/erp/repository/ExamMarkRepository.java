package com.nabarangpur.erp.repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nabarangpur.erp.entity.ExamMark;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ExamMarkRepository extends JpaRepository<ExamMark, Long> {
	@Query("""
			SELECT e FROM ExamMark e
			WHERE e.student.deleted = false
			AND e.examination.id = :examinationId
			AND e.subject.id = :subjectId
			AND e.student.id = :studentId
			""")
			List<ExamMark> findByExaminationIdAndSubjectIdAndStudentId(
			        @Param("examinationId") Long examinationId,
			        @Param("subjectId") Long subjectId,
			        @Param("studentId") Long studentId);


			@Query("""
			SELECT e FROM ExamMark e
			WHERE e.student.deleted = false
			AND e.examination.id = :examinationId
			AND e.subject.id = :subjectId
			""")
			List<ExamMark> findByExaminationIdAndSubjectId(
			        @Param("examinationId") Long examinationId,
			        @Param("subjectId") Long subjectId);


			@Query("""
			SELECT e FROM ExamMark e
			WHERE e.student.deleted = false
			AND e.examination.id = :examinationId
			AND e.subject.id = :subjectId
			AND e.student.id = :studentId
			AND e.component.id = :componentId
			""")
			Optional<ExamMark> findByExaminationIdAndSubjectIdAndStudentIdAndComponentId(
			        @Param("examinationId") Long examinationId,
			        @Param("subjectId") Long subjectId,
			        @Param("studentId") Long studentId,
			        @Param("componentId") Long componentId);


			@Query("""
			SELECT e FROM ExamMark e
			WHERE e.student.deleted = false
			AND e.student.id = :studentId
			""")
			List<ExamMark> findByStudentId(@Param("studentId") Long studentId);
			
			@Transactional
			void deleteByStudentId(Long studentId);
}
