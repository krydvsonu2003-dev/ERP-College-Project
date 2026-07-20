package com.nabarangpur.erp.repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nabarangpur.erp.entity.ResultCard;
import com.nabarangpur.erp.entity.ResultStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResultCardRepository extends JpaRepository<ResultCard, Long> {
	
	@Query("""
			SELECT r FROM ResultCard r
			WHERE r.student.deleted = false
			AND r.student.id = :studentId
			AND r.examination.id = :examinationId
			AND r.subject.id = :subjectId
			""")
			Optional<ResultCard> findByStudentIdAndExaminationIdAndSubjectId(
			        @Param("studentId") Long studentId,
			        @Param("examinationId") Long examinationId,
			        @Param("subjectId") Long subjectId);


			@Query("""
			SELECT r FROM ResultCard r
			WHERE r.student.deleted = false
			AND r.examination.id = :examinationId
			""")
			List<ResultCard> findByExaminationId(@Param("examinationId") Long examinationId);


			@Query("""
			SELECT r FROM ResultCard r
			WHERE r.student.deleted = false
			AND r.student.id = :studentId
			AND r.status = :status
			""")
			List<ResultCard> findByStudentIdAndStatus(
			        @Param("studentId") Long studentId,
			        @Param("status") ResultStatus status);


			@Query("""
			SELECT r FROM ResultCard r
			WHERE r.student.deleted = false
			AND r.student.id = :studentId
			AND r.examination.id = :examinationId
			""")
			List<ResultCard> findByStudentIdAndExaminationId(
			        @Param("studentId") Long studentId,
			        @Param("examinationId") Long examinationId);
			
			@Transactional
			void deleteByStudentId(Long studentId);

				
}
