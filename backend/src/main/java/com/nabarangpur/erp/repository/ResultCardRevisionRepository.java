package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.ResultCardRevision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface ResultCardRevisionRepository extends JpaRepository<ResultCardRevision, Long> {
	@Transactional
	void deleteByResultCardStudentId(Long studentId);
}
