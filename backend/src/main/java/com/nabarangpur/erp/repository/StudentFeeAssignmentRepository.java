package com.nabarangpur.erp.repository;
import org.springframework.transaction.annotation.Transactional;
import com.nabarangpur.erp.entity.StudentFeeAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentFeeAssignmentRepository extends JpaRepository<StudentFeeAssignment, Long> {
    List<StudentFeeAssignment> findByStudentId(Long studentId);
    
    @Transactional
    void deleteByStudentId(Long studentId);
}
