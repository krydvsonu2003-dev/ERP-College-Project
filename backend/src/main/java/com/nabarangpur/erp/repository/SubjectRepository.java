package com.nabarangpur.erp.repository;

import com.nabarangpur.erp.entity.Subject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    List<Subject> findByCourseIdAndSemester(Long courseId, Integer semester);
}
